package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.targetfit;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleContributionView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.AppleTargetFitUnavailableException;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDefinition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDefinition.SugarAcidityTarget;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDistanceView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.out.AppleTargetFitAnalyzer;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.TargetFitTargetUnavailableException;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.CandidateScore;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Contribution;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Coordinates;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Distance;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand.TargetArea;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.ScoreTargetFitUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL from Apple catalog analysis language to CiderAnalysis target-fit contracts. */
@Component
@RequiredArgsConstructor
public final class AppleTargetFitAnalysisAdapter implements AppleTargetFitAnalyzer {

  private static final @NonNull String REFERENCE_TYPE = "apple-variety";

  private final @NonNull ScoreTargetFitUseCase scoreTargetFitUseCase;

  @Override
  public @NonNull Scores score(@NonNull Request request) {
    requireNonNull(request, "apple target fit request cannot be null");
    final var target = request.target();
    final var command = new TargetFitCommand(
        new TargetFitCommand.TargetObjective(
            toTarget(target.sugarAcidity()),
            target.tannin()
        ),
        toConfiguration(request),
        TargetFitCommand.IntervalPolicy.MIDPOINT,
        request.candidates().stream()
            .map(candidate -> new TargetFitCommand.Candidate(
                new TargetFitCandidateReference(REFERENCE_TYPE, candidate.reference()),
                new TargetFitCommand.Characteristics(
                    candidate.characteristics().specificGravity(),
                    candidate.characteristics().titratableAcidity(),
                    candidate.characteristics().tannin()
                )
            ))
            .toList()
    );
    final TargetFitCandidateScoresView result;
    try {
      result = scoreTargetFitUseCase.score(command);
    } catch (TargetFitTargetUnavailableException exception) {
      throw new AppleTargetFitUnavailableException(exception.getMessage(), exception);
    }
    return new Scores(
        Normalizer.valueOf(result.configuration().normalizer().name()),
        result.scores().stream().map(AppleTargetFitAnalysisAdapter::toScore).toList()
    );
  }

  private static @NonNull TargetArea toTarget(
      @NonNull SugarAcidityTarget target
  ) {
    return switch (target) {
      case AppleTargetDefinition.PredefinedTarget(var zone) -> new TargetFitCommand.PredefinedTarget(
          JolicoeurSugarAcidityZoneCode.valueOf(zone.name())
      );
      case AppleTargetDefinition.CustomRectangleTarget(var specificGravity, var titratableAcidity) ->
          new TargetFitCommand.CustomRectangleTarget(specificGravity, titratableAcidity);
    };
  }

  private static @NonNull TargetFitScoringConfiguration toConfiguration(
      @NonNull Request request
  ) {
    final var configuration = request.configuration();
    final var weights = configuration.weights();
    return new TargetFitScoringConfiguration(
        TargetFitScoringConfiguration.Normalizer.valueOf(configuration.normalizer().name()),
        TargetFitScoringConfiguration.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        new ObjectiveWeights(
            weights.specificGravity(),
            weights.titratableAcidity(),
            weights.tannin()
        )
    );
  }

  private static @NonNull Score toScore(@NonNull CandidateScore score) {
    return new Score(
        score.reference().id(),
        toDistance(score.distance()),
        toContribution(score.contribution())
    );
  }

  private static @NonNull AppleTargetDistanceView toDistance(@NonNull Distance distance) {
    return new AppleTargetDistanceView(
        AppleTargetDistanceView.Status.valueOf(distance.status().name()),
        toCoordinates(distance.point()),
        toCoordinates(distance.nearestTargetPoint()),
        distance.tannin(),
        distance.nearestTargetTannin(),
        distance.value(),
        distance.matchPercent(),
        AppleTargetDistanceView.FitStatus.valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        AppleTargetDistanceView.ObjectiveFunction.valueOf(distance.objectiveFunction().name()),
        AppleTargetDistanceView.Metric.valueOf(distance.metric().name()),
        AppleTargetDistanceView.TargetKind.valueOf(distance.targetKind().name()),
        AppleTargetDistanceView.GeometryShape.valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> AppleTargetDistanceView.EvaluationAxis.valueOf(axis.name()))
            .collect(Collectors.toUnmodifiableSet())
    );
  }

  private static AppleTargetDistanceView.@NonNull Coordinates toCoordinates(
      @NonNull Coordinates coordinates
  ) {
    return new AppleTargetDistanceView.Coordinates(
        coordinates.specificGravity(),
        coordinates.titratableAcidity()
    );
  }

  private static @NonNull AppleContributionView toContribution(@NonNull Contribution contribution) {
    return new AppleContributionView(
        contribution.roles().stream()
            .map(role -> AppleContributionView.Role.valueOf(role.name()))
            .toList(),
        contribution.evidence().stream()
            .map(evidence -> new AppleContributionView.Evidence(
                AppleTargetDistanceView.EvaluationAxis.valueOf(evidence.axis().name()),
                evidence.sourceValue(),
                evidence.targetCoordinate(),
                evidence.deviationFromTarget(),
                AppleContributionView.Direction.valueOf(evidence.direction().name()),
                AppleContributionView.Effect.valueOf(evidence.effect().name()),
                AppleContributionView.Severity.valueOf(evidence.severity().name())
            ))
            .toList(),
        contribution.missingAxes().stream()
            .map(axis -> AppleTargetDistanceView.EvaluationAxis.valueOf(axis.name()))
            .collect(Collectors.toUnmodifiableSet())
    );
  }
}
