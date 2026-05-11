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
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockSampleBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleBlendProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleBlendProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockSampleBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockSampleBlendProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockSampleBlendProfileTargetUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockSampleSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
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
public final class ScoreFeedstockSampleBlendProfileTargetService
    implements ScoreFeedstockSampleBlendProfileTargetUseCase {

  private static final @NonNull String REFERENCE_TYPE = "feedstock-sample";

  private final @NonNull FeedstockSampleQueryRepository feedstockSampleQueryRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor zoneAssessor;
  private final @NonNull SugarAcidityTargetResolver targetResolver;
  private final @NonNull ScoreBlendProfileDistanceCandidatesUseCase scoreBlendProfileDistanceCandidatesUseCase;

  @Override
  public @NonNull FeedstockSampleBlendProfileTargetScores score(
      @NonNull ScoreFeedstockSampleBlendProfileTargetCommand command
  ) {
    requireNonNull(command, "score feedstock sample blend profile target command cannot be null");

    final var candidates = feedstockSampleQueryRepository.findLatestSugarAcidityPointCandidates(
            new LatestFeedstockSampleSugarAcidityPointCandidateQuery(
                command.ciderMakerId(),
                command.filters().batch(),
                command.filters().season(),
                command.limit()
            )
        )
        .stream()
        .map(sample -> toCandidateIfScorable(sample, command))
        .flatMap(Optional::stream)
        .toList();

    final Map<@NonNull String, @NonNull ScorableFeedstockSampleCandidate> candidatesByReference = candidates.stream()
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
                .map(ScorableFeedstockSampleCandidate::candidate)
                .toList()
        )
    );

    return new FeedstockSampleBlendProfileTargetScores(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference().id()),
                  "feedstock sample candidate is missing for score"
              );
              final var sample = candidate.sample();
              return new FeedstockSampleBlendProfileTargetScore(
                  sample.sampleId(),
                  command.ciderMakerId(),
                  sample.batchId(),
                  sample.name(),
                  candidate.candidate().point(),
                  score.distance()
              );
            })
            .toList()
    );
  }

  private @NonNull Optional<@NonNull ScorableFeedstockSampleCandidate> toCandidateIfScorable(
      @NonNull FeedstockSample sample,
      @NonNull ScoreFeedstockSampleBlendProfileTargetCommand command
  ) {
    requireNonNull(sample, "feedstock sample cannot be null");
    final var axes = axisExtractor.extract(sample.chemicalProfile());
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

    return Optional.of(new ScorableFeedstockSampleCandidate(
        sample,
        new BlendProfileDistanceCandidate(
            referenceId(sample),
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
      @NonNull ScoreFeedstockSampleBlendProfileTargetCommand command
  ) {
    final SugarAcidityTarget target = switch (command.target()) {
      case PredefinedFeedstockSampleBlendProfileTargetSelection predefined -> targetResolver.resolvePredefinedZone(predefined.zone()); // NOSONAR
      case CustomRectangleFeedstockSampleBlendProfileTargetSelection custom -> new CustomRectangleSugarAcidityTarget(custom.geometry()); // NOSONAR
    };

    return command.tanninTarget() == null
        ? BlendProfileObjective.of(target)
        : BlendProfileObjective.withTanninTarget(target, command.tanninTarget()); // NOSONAR
  }

  private static @NonNull BlendProfileDistanceCandidateReference referenceId(
      @NonNull FeedstockSample sample
  ) {
    requireNonNull(sample, "feedstock sample cannot be null");
    return new BlendProfileDistanceCandidateReference(
        REFERENCE_TYPE,
        sample.sampleId().value().toString()
    );
  }

  private record ScorableFeedstockSampleCandidate(
      @NonNull FeedstockSample sample,
      @NonNull BlendProfileDistanceCandidate candidate
  ) {

    private ScorableFeedstockSampleCandidate {
      requireNonNull(sample, "feedstock sample cannot be null");
      requireNonNull(candidate, "blend profile distance candidate cannot be null");
    }
  }
}
