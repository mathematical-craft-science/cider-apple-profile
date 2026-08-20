package org.craft.mcs.cap.core.cideranalysis.targetfit.application.usecase;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.TargetFitTargetUnavailableException;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneId;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Contribution;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Coordinates;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Distance;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration.ObjectiveFunction;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand.Characteristics;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand.TargetArea;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand.TargetObjective;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.ScoreTargetFitUseCase;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution.CompositionContributionRoleAssessmentRequest;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetIntervalPolicy;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetDistanceOrdering;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.ProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.SugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.SugarAcidityTargetResolutionException;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.TanninTargetRange;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Maps the published target-fit contract to internal scoring and contribution policies. */
@Component
@RequiredArgsConstructor
public final class ScoreTargetFitService implements ScoreTargetFitUseCase {

  private final @NonNull SugarAcidityTargetResolver targetResolver;
  private final @NonNull ProfileAxisNormalizerSelector normalizerSelector;
  private final @NonNull ProfileTargetObjectiveFunctionFactory objectiveFunctionFactory;
  private final @NonNull CompositionContributionRoleAssessor contributionRoleAssessor;

  @Override
  public @NonNull TargetFitCandidateScoresView score(
      @NonNull TargetFitCommand command
  ) {
    requireNonNull(command, "target fit command cannot be null");
    try {
      return scoreValidated(command);
    } catch (SugarAcidityTargetResolutionException exception) {
      throw new TargetFitTargetUnavailableException(exception.getMessage(), exception);
    }
  }

  private @NonNull TargetFitCandidateScoresView scoreValidated(
      @NonNull TargetFitCommand command
  ) {
    final var objective = toDomain(command.objective());
    final var configuration = command.configuration();
    final var objectiveFunction = objectiveFunctionFactory.create(
        ProfileTargetObjectiveFunctionCode.valueOf(configuration.objectiveFunction().name()),
        normalizerSelector.select(ProfileAxisNormalizerCode.valueOf(configuration.normalizer().name())),
        toDomain(configuration.weights())
    );
    final var intervalPolicy = ProfileTargetIntervalPolicy.valueOf(command.intervalPolicy().name());
    final var scores = command.candidates().stream()
        .map(candidate -> {
          final var distance = objectiveFunction.distanceTo(
              toDomain(candidate.characteristics()),
              intervalPolicy,
              objective
          );
          final var contribution = contributionRoleAssessor.assess(
              new CompositionContributionRoleAssessmentRequest(distance)
          );
          return new ScoredCandidate(candidate.reference(), distance, contribution);
        })
        .sorted(Comparator.comparing(
            ScoredCandidate::distance,
            ProfileTargetDistanceOrdering::compare
        ))
        .map(ScoreTargetFitService::toView)
        .toList();

    return new TargetFitCandidateScoresView(configuration, scores);
  }

  private @NonNull ProfileTargetObjective toDomain(
      @NonNull TargetObjective objective
  ) {
    requireNonNull(objective, "target objective cannot be null");
    final var target = toDomain(objective.sugarAcidityTarget());
    final var tanninTarget = objective.tanninTarget();
    return tanninTarget == null
        ? ProfileTargetObjective.of(target)
        : ProfileTargetObjective.withTanninTarget(target, new TanninTargetRange(tanninTarget));
  }

  private @NonNull SugarAcidityTarget toDomain(
      @NonNull TargetArea target
  ) {
    return switch (requireNonNull(target, "target area cannot be null")) {
      case TargetFitCommand.PredefinedTarget(var zone) -> targetResolver.resolvePredefinedZone(
          JolicoeurSugarAcidityZoneId.valueOf(zone.name())
      );
      case TargetFitCommand.CustomRectangleTarget(var specificGravity, var titratableAcidity) ->
          new CustomRectangleSugarAcidityTarget(
              new RectangleSugarAcidityTargetGeometry(specificGravity, titratableAcidity)
          );
    };
  }

  private static @NonNull ProfileTargetObjectiveWeights toDomain(
      @NonNull ObjectiveWeights weights
  ) {
    requireNonNull(weights, "target objective weights cannot be null");
    return new ProfileTargetObjectiveWeights(
        weights.specificGravity(),
        weights.titratableAcidity(),
        weights.tannin()
    );
  }

  private static @NonNull ProfileTargetScoringInput toDomain(
      @NonNull Characteristics characteristics
  ) {
    requireNonNull(characteristics, "target characteristics cannot be null");
    return new ProfileTargetScoringInput(
        characteristics.specificGravity(),
        characteristics.titratableAcidity(),
        characteristics.tannin()
    );
  }

  private static @NonNull Distance toView(
      @NonNull ProfileTargetDistance distance
  ) {
    requireNonNull(distance, "target distance cannot be null");
    final var fit = distance.fitAssessment();
    return new TargetFitCandidateScoresView.Distance(
        TargetFitCandidateScoresView.DistanceStatus.valueOf(distance.status().name()),
        toView(distance.point()),
        toView(distance.nearestTargetPoint()),
        new TargetFitCandidateScoresView.TargetPosition(
            TargetFitCandidateScoresView.AxisPosition.valueOf(
                distance.sugarAcidityTargetPosition().specificGravity().name()
            ),
            TargetFitCandidateScoresView.AxisPosition.valueOf(
                distance.sugarAcidityTargetPosition().titratableAcidity().name()
            )
        ),
        distance.tannin(),
        distance.nearestTargetTannin(),
        distance.value(),
        fit.matchPercent(),
        TargetFitCandidateScoresView.FitStatus.valueOf(fit.status().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        ObjectiveFunction.valueOf(distance.objectiveFunction().name()),
        TargetFitCandidateScoresView.DistanceMetric.valueOf(distance.metric().name()),
        new ObjectiveWeights(
            distance.weights().specificGravity(),
            distance.weights().titratableAcidity(),
            distance.weights().tannin()
        ),
        TargetFitCandidateScoresView.TargetKind.valueOf(distance.targetKind().name()),
        TargetFitCandidateScoresView.GeometryShape.valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> TargetFitCandidateScoresView.EvaluationAxis.valueOf(axis.name()))
            .collect(java.util.stream.Collectors.toUnmodifiableSet())
    );
  }

  private static @NonNull Coordinates toView(
      @NonNull SugarAcidityGraphPoint point
  ) {
    requireNonNull(point, "SG/TA point cannot be null");
    return new TargetFitCandidateScoresView.Coordinates(
        point.specificGravity(),
        point.titratableAcidity()
    );
  }

  private static @NonNull Contribution toView(
      @NonNull CompositionContributionRoleAssessment contribution
  ) {
    requireNonNull(contribution, "target contribution cannot be null");
    return new TargetFitCandidateScoresView.Contribution(
        contribution.roles().stream()
            .map(role -> TargetFitCandidateScoresView.ContributionRole.valueOf(role.name()))
            .toList(),
        contribution.evidence().stream()
            .map(evidence -> new TargetFitCandidateScoresView.ContributionEvidence(
                TargetFitCandidateScoresView.EvaluationAxis.valueOf(evidence.axis().name()),
                evidence.sourceValue().asBigDecimal(),
                evidence.targetCoordinate().asBigDecimal(),
                evidence.deviationFromTarget(),
                TargetFitCandidateScoresView.EvidenceDirection.valueOf(evidence.direction().name()),
                TargetFitCandidateScoresView.EvidenceEffect.valueOf(evidence.effect().name()),
                TargetFitCandidateScoresView.EvidenceSeverity.valueOf(evidence.severity().name())
            ))
            .toList(),
        contribution.missingAxes().stream()
            .map(axis -> TargetFitCandidateScoresView.EvaluationAxis.valueOf(axis.name()))
            .collect(java.util.stream.Collectors.toUnmodifiableSet())
    );
  }

  private static TargetFitCandidateScoresView.@NonNull CandidateScore toView(
      @NonNull ScoredCandidate candidate
  ) {
    requireNonNull(candidate, "scored target candidate cannot be null");
    return new TargetFitCandidateScoresView.CandidateScore(
        candidate.reference(),
        toView(candidate.distance()),
        toView(candidate.contribution())
    );
  }

  private record ScoredCandidate(
      @NonNull TargetFitCandidateReference reference,
      @NonNull ProfileTargetDistance distance,
      @NonNull CompositionContributionRoleAssessment contribution
  ) {

    private ScoredCandidate {
      requireNonNull(reference, "scored target candidate reference cannot be null");
      requireNonNull(distance, "scored target candidate distance cannot be null");
      requireNonNull(contribution, "scored target candidate contribution cannot be null");
    }
  }
}
