package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchProfileTargetUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class ScoreFeedstockBatchProfileTargetService
    implements ScoreFeedstockBatchProfileTargetUseCase {

  private static final @NonNull String REFERENCE_TYPE = "feedstock-batch";

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor zoneAssessor;
  private final @NonNull SugarAcidityTargetResolver targetResolver;
  private final @NonNull ScoreProfileTargetDistanceCandidatesUseCase scoreProfileTargetDistanceCandidatesUseCase;

  @Override
  public @NonNull FeedstockBatchProfileTargetScores score(
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    requireNonNull(command, "score feedstock batch profile target command cannot be null");

    final var candidates = feedstockMeasurementQueryRepository.findLatestSugarAcidityPointCandidates(
            new LatestFeedstockMeasurementSugarAcidityPointCandidateQuery(
                command.ciderMakerId(),
                command.filters().batch(),
                command.filters().season(),
                null,
                command.limit()
            )
        )
        .stream()
        .map(measurement -> toCandidateIfScorable(measurement, command))
        .flatMap(Optional::stream)
        .toList();

    final Map<@NonNull String, @NonNull ScorableFeedstockMeasurementCandidate> candidatesByReference = candidates.stream()
        .collect(Collectors.toMap(
            candidate -> candidate.candidate().reference().id(),
            Function.identity()
        ));
    final var objective = objective(command);
    final var scores = scoreProfileTargetDistanceCandidatesUseCase.score(
        new ScoreProfileTargetDistanceCandidatesCommand(
            command.purpose(),
            objective,
            command.weights(),
            candidates.stream()
                .map(ScorableFeedstockMeasurementCandidate::candidate)
                .toList()
        )
    );

    return new FeedstockBatchProfileTargetScores(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference().id()),
                  "feedstock measurement candidate is missing for score"
              );
              final var measurement = candidate.measurement();
              return new FeedstockBatchProfileTargetScore(
                  measurement.batchId(),
                  command.ciderMakerId(),
                  measurement.measurementId(),
                  measurement.name(),
                  candidate.candidate().point(),
                  score.distance()
              );
            })
            .toList()
    );
  }

  private @NonNull Optional<@NonNull ScorableFeedstockMeasurementCandidate> toCandidateIfScorable(
      @NonNull FeedstockMeasurement measurement,
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    final var axes = axisExtractor.extract(measurement.chemicalProfile());
    if (!capabilityAssessor.assess(axes, ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable()) {
      return Optional.empty();
    }
    if (!matchesCapability(axes, command.filters().capability())) {
      return Optional.empty();
    }

    final var assessment = zoneAssessor.assess(axes).orElse(null); // NOSONAR
    if (assessment == null || !matchesSugarAcidityZone(assessment, command.filters().sugarAcidityZone())) {
      return Optional.empty();
    }

    return Optional.of(new ScorableFeedstockMeasurementCandidate(
        measurement,
        new ProfileTargetDistanceCandidate(
            referenceId(measurement),
            assessment.point()
        )
    ));
  }

  private boolean matchesCapability(
      @NonNull ChemicalProfileAxes axes,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }
    return capabilityAssessor.assess(axes, criteria.capability()).capable() == criteria.capable(); // NOSONAR
  }

  private static boolean matchesSugarAcidityZone(
      @NonNull SugarAcidityZoneAssessment assessment,
      @Nullable SugarAcidityZoneCriteria criteria
  ) {
    return criteria == null || criteria.zones().contains(assessment.zone());
  }

  private @NonNull ProfileTargetObjective objective(
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    final SugarAcidityTarget target = switch (command.target()) {
      case PredefinedFeedstockProfileTargetSelection predefined -> targetResolver.resolvePredefinedZone(predefined.zone()); // NOSONAR
      case CustomRectangleFeedstockProfileTargetSelection custom -> new CustomRectangleSugarAcidityTarget(custom.geometry()); // NOSONAR
    };

    return command.tanninTarget() == null
        ? ProfileTargetObjective.of(target)
        : ProfileTargetObjective.withTanninTarget(target, command.tanninTarget()); // NOSONAR
  }

  private static @NonNull ProfileTargetDistanceCandidateReference referenceId(
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new ProfileTargetDistanceCandidateReference(
        REFERENCE_TYPE,
        measurement.batchId().value().toString()
    );
  }

  private record ScorableFeedstockMeasurementCandidate(
      @NonNull FeedstockMeasurement measurement,
      @NonNull ProfileTargetDistanceCandidate candidate
  ) {

    private ScorableFeedstockMeasurementCandidate {
      requireNonNull(measurement, "feedstock measurement cannot be null");
      requireNonNull(candidate, "profile target distance candidate cannot be null");
    }
  }
}
