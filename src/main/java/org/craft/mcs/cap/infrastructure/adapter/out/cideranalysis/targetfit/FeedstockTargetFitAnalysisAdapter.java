package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.targetfit;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.FeedstockTargetFitUnavailableException;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveFunction;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.out.FeedstockTargetFitAnalyzer;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL from Feedstock planning target language to CiderAnalysis target-fit contracts. */
@Component
@RequiredArgsConstructor
public final class FeedstockTargetFitAnalysisAdapter implements FeedstockTargetFitAnalyzer {

  private static final @NonNull String REFERENCE_TYPE = "feedstock-batch";

  private final @NonNull ScoreTargetFitUseCase scoreTargetFitUseCase;

  @Override
  public @NonNull Scores score(@NonNull Request request) {
    requireNonNull(request, "feedstock target fit request cannot be null");
    final var command = new TargetFitCommand(
        new TargetFitCommand.TargetObjective(
            toTarget(request.target()),
            request.tanninTarget()
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
      throw new FeedstockTargetFitUnavailableException(exception.getMessage(), exception);
    }

    return new Scores(
        Normalizer.valueOf(result.configuration().normalizer().name()),
        result.scores().stream().map(FeedstockTargetFitAnalysisAdapter::toScore).toList()
    );
  }

  private static @NonNull TargetArea toTarget(
      @NonNull FeedstockProfileTargetSelection selection
  ) {
    return switch (requireNonNull(selection, "feedstock target selection cannot be null")) {
      case PredefinedFeedstockProfileTargetSelection(var zone) -> new TargetFitCommand.PredefinedTarget(
          JolicoeurSugarAcidityZoneCode.valueOf(zone.name())
      );
      case CustomRectangleFeedstockProfileTargetSelection(var specificGravity, var titratableAcidity) ->
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

  private static @NonNull Score toScore(
      @NonNull CandidateScore score
  ) {
    requireNonNull(score, "target candidate score cannot be null");
    return new Score(
        score.reference().id(),
        toDistance(score.distance()),
        toContribution(score.contribution())
    );
  }

  private static @NonNull FeedstockTargetDistanceView toDistance(
      @NonNull Distance distance
  ) {
    return new FeedstockTargetDistanceView(
        FeedstockTargetDistanceView.Status.valueOf(distance.status().name()),
        toCoordinates(distance.point()),
        toCoordinates(distance.nearestTargetPoint()),
        distance.tannin(),
        distance.nearestTargetTannin(),
        distance.value(),
        distance.matchPercent(),
        FeedstockTargetDistanceView.FitStatus.valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        ObjectiveFunction.valueOf(distance.objectiveFunction().name()),
        FeedstockTargetDistanceView.Metric.valueOf(distance.metric().name()),
        FeedstockTargetDistanceView.TargetKind.valueOf(distance.targetKind().name()),
        FeedstockTargetDistanceView.GeometryShape.valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> FeedstockTargetDistanceView.EvaluationAxis.valueOf(axis.name()))
            .collect(Collectors.toUnmodifiableSet())
    );
  }

  private static FeedstockTargetDistanceView.@NonNull Coordinates toCoordinates(
      @NonNull Coordinates coordinates
  ) {
    return new FeedstockTargetDistanceView.Coordinates(
        coordinates.specificGravity(),
        coordinates.titratableAcidity()
    );
  }

  private static @NonNull FeedstockContributionView toContribution(
      @NonNull Contribution contribution
  ) {
    return new FeedstockContributionView(
        contribution.roles().stream()
            .map(role -> FeedstockContributionView.Role.valueOf(role.name()))
            .toList(),
        contribution.evidence().stream()
            .map(evidence -> new FeedstockContributionView.Evidence(
                FeedstockTargetDistanceView.EvaluationAxis.valueOf(evidence.axis().name()),
                evidence.sourceValue(),
                evidence.targetCoordinate(),
                evidence.deviationFromTarget(),
                FeedstockContributionView.Direction.valueOf(evidence.direction().name()),
                FeedstockContributionView.Effect.valueOf(evidence.effect().name()),
                FeedstockContributionView.Severity.valueOf(evidence.severity().name())
            ))
            .toList(),
        contribution.missingAxes().stream()
            .map(axis -> FeedstockTargetDistanceView.EvaluationAxis.valueOf(axis.name()))
            .collect(Collectors.toUnmodifiableSet())
    );
  }
}
