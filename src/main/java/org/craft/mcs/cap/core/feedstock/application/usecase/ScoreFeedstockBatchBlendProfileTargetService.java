package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileDistanceCandidate;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileDistanceCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ScoreBlendProfileDistanceCandidatesCommand;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ScoreBlendProfileDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjective;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchBlendProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchBlendProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchBlendProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchBlendProfileTargetUseCase;
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
public final class ScoreFeedstockBatchBlendProfileTargetService
    implements ScoreFeedstockBatchBlendProfileTargetUseCase {

  private static final @NonNull String REFERENCE_TYPE = "feedstock-batch";

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor zoneAssessor;
  private final @NonNull SugarAcidityTargetResolver targetResolver;
  private final @NonNull ScoreBlendProfileDistanceCandidatesUseCase scoreBlendProfileDistanceCandidatesUseCase;

  @Override
  public @NonNull FeedstockBatchBlendProfileTargetScores score(
      @NonNull ScoreFeedstockBatchBlendProfileTargetCommand command
  ) {
    requireNonNull(command, "score feedstock batch blend profile target command cannot be null");

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
    final var scores = scoreBlendProfileDistanceCandidatesUseCase.score(
        new ScoreBlendProfileDistanceCandidatesCommand(
            command.purpose(),
            objective,
            command.weights(),
            candidates.stream()
                .map(ScorableFeedstockMeasurementCandidate::candidate)
                .toList()
        )
    );

    return new FeedstockBatchBlendProfileTargetScores(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference().id()),
                  "feedstock measurement candidate is missing for score"
              );
              final var measurement = candidate.measurement();
              return new FeedstockBatchBlendProfileTargetScore(
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
      @NonNull ScoreFeedstockBatchBlendProfileTargetCommand command
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
        new BlendProfileDistanceCandidate(
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

  private @NonNull BlendProfileObjective objective(
      @NonNull ScoreFeedstockBatchBlendProfileTargetCommand command
  ) {
    final SugarAcidityTarget target = switch (command.target()) {
      case PredefinedFeedstockBatchBlendProfileTargetSelection predefined -> targetResolver.resolvePredefinedZone(predefined.zone()); // NOSONAR
      case CustomRectangleFeedstockBatchBlendProfileTargetSelection custom -> new CustomRectangleSugarAcidityTarget(custom.geometry()); // NOSONAR
    };

    return command.tanninTarget() == null
        ? BlendProfileObjective.of(target)
        : BlendProfileObjective.withTanninTarget(target, command.tanninTarget()); // NOSONAR
  }

  private static @NonNull BlendProfileDistanceCandidateReference referenceId(
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new BlendProfileDistanceCandidateReference(
        REFERENCE_TYPE,
        measurement.batchId().value().toString()
    );
  }

  private record ScorableFeedstockMeasurementCandidate(
      @NonNull FeedstockMeasurement measurement,
      @NonNull BlendProfileDistanceCandidate candidate
  ) {

    private ScorableFeedstockMeasurementCandidate {
      requireNonNull(measurement, "feedstock measurement cannot be null");
      requireNonNull(candidate, "blend profile distance candidate cannot be null");
    }
  }
}
