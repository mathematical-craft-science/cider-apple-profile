package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetComparison;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetIntervalPolicy;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.ProfileAxisNormalizer;
import org.jspecify.annotations.NonNull;


/**
 * Evaluates target-area geometry once and delegates normalized axis aggregation to the selected metric.
 */
public final class TargetAreaProfileTargetObjectiveFunction
    implements ProfileTargetObjectiveFunction {

  private final @NonNull ProfileTargetObjectiveFunctionCode code;
  private final @NonNull ProfileDistanceMetric metric;
  private final @NonNull ProfileAxisNormalizer normalizer;
  private final @NonNull ProfileTargetObjectiveWeights weights;

  private TargetAreaProfileTargetObjectiveFunction(
      @NonNull ProfileTargetObjectiveFunctionCode code,
      @NonNull ProfileDistanceMetric metric,
      @NonNull ProfileAxisNormalizer normalizer,
      @NonNull ProfileTargetObjectiveWeights weights
  ) {
    this.code = requireNonNull(code, "profile target objective function code cannot be null");
    this.metric = requireNonNull(metric, "profile distance metric cannot be null");
    this.normalizer = requireNonNull(normalizer, "profile axis normalizer cannot be null");
    this.weights = requireNonNull(weights, "profile target objective weights cannot be null");
  }

  public static @NonNull TargetAreaProfileTargetObjectiveFunction euclidean(
      @NonNull ProfileAxisNormalizer normalizer
  ) {
    return euclidean(normalizer, ProfileTargetObjectiveWeights.balanced());
  }

  public static @NonNull TargetAreaProfileTargetObjectiveFunction euclidean(
      @NonNull ProfileAxisNormalizer normalizer,
      @NonNull ProfileTargetObjectiveWeights weights
  ) {
    return new TargetAreaProfileTargetObjectiveFunction(
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        ProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        normalizer,
        weights
    );
  }

  public static @NonNull TargetAreaProfileTargetObjectiveFunction manhattan(
      @NonNull ProfileAxisNormalizer normalizer,
      @NonNull ProfileTargetObjectiveWeights weights
  ) {
    return new TargetAreaProfileTargetObjectiveFunction(
        ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA,
        ProfileDistanceMetric.NORMALIZED_WEIGHTED_MANHATTAN,
        normalizer,
        weights
    );
  }

  @Override
  public @NonNull ProfileTargetObjectiveFunctionCode code() {
    return code;
  }

  @Override
  public @NonNull ProfileTargetDistance distanceTo(
      @NonNull ProfileTargetScoringInput input,
      @NonNull ProfileTargetIntervalPolicy intervalPolicy,
      @NonNull ProfileTargetObjective objective
  ) {
    requireNonNull(input, "profile target scoring input cannot be null");
    requireNonNull(intervalPolicy, "profile target interval policy cannot be null");
    requireNonNull(objective, "profile target objective cannot be null");
    final var point = input.representativePoint(intervalPolicy);
    return score(objective.compare(point, this::sugarAcidityDistance), objective);
  }

  private @NonNull ProfileTargetDistance score(
      @NonNull ProfileTargetComparison comparison,
      @NonNull ProfileTargetObjective objective
  ) {
    final var target = objective.sugarAcidityTarget();
    if (!comparison.comparable()) {
      return new ProfileTargetDistance(
          ProfileTargetDistanceStatus.UNSCORABLE,
          comparison.point(),
          comparison.nearestTargetPoint(),
          comparison.sugarAcidityTargetPosition(),
          comparison.tannin(),
          comparison.nearestTargetTannin(),
          null,
          comparison.insideSugarAcidityTarget(),
          false,
          code,
          metric,
          weights,
          target.kind(),
          target.geometry().shape(),
          comparison.missingAxes()
      );
    }

    return new ProfileTargetDistance(
        ProfileTargetDistanceStatus.SCORED,
        comparison.point(),
        comparison.nearestTargetPoint(),
        comparison.sugarAcidityTargetPosition(),
        comparison.tannin(),
        comparison.nearestTargetTannin(),
        distance(comparison),
        comparison.insideSugarAcidityTarget(),
        comparison.insideTarget(),
        code,
        metric,
        weights,
        target.kind(),
        target.geometry().shape(),
        comparison.missingAxes()
    );
  }

  private @NonNull BigDecimal distance(
      @NonNull ProfileTargetComparison comparison
  ) {
    final var point = comparison.point();
    final var nearestTargetPoint = comparison.nearestTargetPoint();
    final var tannin = comparison.tannin();
    final var nearestTargetTannin = comparison.nearestTargetTannin();
    return metric.aggregate(
        new NormalizedProfileDistanceVector(
            normalizedDelta(
                ProfileEvaluationAxis.SPECIFIC_GRAVITY,
                point.specificGravity().asBigDecimal(),
                nearestTargetPoint.specificGravity().asBigDecimal()
            ),
            normalizedDelta(
                ProfileEvaluationAxis.TITRATABLE_ACIDITY,
                point.titratableAcidity().asBigDecimal(),
                nearestTargetPoint.titratableAcidity().asBigDecimal()
            ),
            tannin == null || nearestTargetTannin == null
            ? BigDecimal.ZERO
            : normalizedDelta(
                ProfileEvaluationAxis.TANNIN,
                tannin.asBigDecimal(),
                nearestTargetTannin.asBigDecimal()
            )
        ),
        weights
    );
  }

  private @NonNull BigDecimal sugarAcidityDistance(
      @NonNull SugarAcidityGraphPoint source,
      @NonNull SugarAcidityGraphPoint target
  ) {
    return metric.aggregate(
        new NormalizedProfileDistanceVector(
            normalizedDelta(
                ProfileEvaluationAxis.SPECIFIC_GRAVITY,
                source.specificGravity().asBigDecimal(),
                target.specificGravity().asBigDecimal()
            ),
            normalizedDelta(
                ProfileEvaluationAxis.TITRATABLE_ACIDITY,
                source.titratableAcidity().asBigDecimal(),
                target.titratableAcidity().asBigDecimal()
            ),
            BigDecimal.ZERO
        ),
        weights
    );
  }

  private @NonNull BigDecimal normalizedDelta(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value,
      @NonNull BigDecimal targetValue
  ) {
    return normalizer.normalize(axis, value)
        .subtract(normalizer.normalize(axis, targetValue))
        .abs();
  }
}
