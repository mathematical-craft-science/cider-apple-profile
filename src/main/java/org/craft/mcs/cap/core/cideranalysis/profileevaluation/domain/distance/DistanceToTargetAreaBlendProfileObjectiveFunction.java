package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTarget;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Scores a point by normalized weighted distance to the nearest target area boundary.
 */
public final class DistanceToTargetAreaBlendProfileObjectiveFunction implements BlendProfileObjectiveFunction {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull BlendProfileAxisNormalizer normalizer;
  private final @NonNull BlendProfileObjectiveWeights weights;

  public DistanceToTargetAreaBlendProfileObjectiveFunction(
      @NonNull BlendProfileAxisNormalizer normalizer
  ) {
    this(normalizer, BlendProfileObjectiveWeights.balanced());
  }

  public DistanceToTargetAreaBlendProfileObjectiveFunction(
      @NonNull BlendProfileAxisNormalizer normalizer,
      @NonNull BlendProfileObjectiveWeights weights
  ) {
    this.normalizer = requireNonNull(normalizer, "blend profile axis normalizer cannot be null");
    this.weights = requireNonNull(weights, "sugar-acidity objective weights cannot be null");
  }

  @Override
  public @NonNull BlendProfileObjectiveFunctionCode code() {
    return BlendProfileObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA;
  }

  @Override
  public @NonNull BlendProfileTargetDistance distanceTo(
      @NonNull SugarAcidityCoordinates point,
      @NonNull SugarAcidityTarget target
  ) {
    requireNonNull(point, "sugar-acidity point cannot be null");
    requireNonNull(target, "sugar-acidity target cannot be null");
    return score(point, null, BlendProfileObjective.of(target));
  }

  @Override
  public @NonNull BlendProfileTargetDistance distanceTo(
      @NonNull SugarAcidityProfilePoint point,
      @NonNull BlendProfileObjective objective
  ) {
    requireNonNull(point, "sugar-acidity profile point cannot be null");
    requireNonNull(objective, "sugar-acidity profile objective cannot be null");
    return score(
        SugarAcidityCoordinates.from(point),
        point.tannin() == null ? null : point.tannin().value(), // NOSONAR
        objective
    );
  }

  private @NonNull BlendProfileTargetDistance score(
      @NonNull SugarAcidityCoordinates point,
      @Nullable BigDecimal tannin,
      @NonNull BlendProfileObjective objective
  ) {
    final var target = objective.sugarAcidityTarget();
    final var insideSugarAcidityTarget = target.geometry().contains(point);
    final var nearestTargetPoint = insideSugarAcidityTarget ? point : target.geometry().nearestPointTo(point);
    final var tanninTarget = objective.tanninTarget();
    if (tanninTarget != null && tannin == null) {
      return new BlendProfileTargetDistance(
          BlendProfileTargetDistanceStatus.UNSCORABLE,
          point,
          nearestTargetPoint,
          null,
          null,
          null,
          insideSugarAcidityTarget,
          false,
          code(),
          BlendProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
          weights,
          target.kind(),
          target.geometry().shape(),
          List.of(BlendProfileAxis.TANNIN)
      );
    }

    final var nearestTargetTannin = tanninTarget == null ? null : tanninTarget.nearestValueTo(tannin);
    final var insideTanninTarget = tanninTarget == null || tanninTarget.contains(tannin);
    final var distance = normalizedWeightedDistance(point, nearestTargetPoint, tannin, nearestTargetTannin);

    return new BlendProfileTargetDistance(
        BlendProfileTargetDistanceStatus.SCORED,
        point,
        nearestTargetPoint,
        tannin,
        nearestTargetTannin,
        distance,
        insideSugarAcidityTarget,
        insideSugarAcidityTarget && insideTanninTarget,
        code(),
        BlendProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        weights,
        target.kind(),
        target.geometry().shape(),
        List.of()
    );
  }

  private @NonNull BigDecimal normalizedWeightedDistance(
      @NonNull SugarAcidityCoordinates point,
      @NonNull SugarAcidityCoordinates nearestTargetPoint,
      @Nullable BigDecimal tannin,
      @Nullable BigDecimal nearestTargetTannin
  ) {
    final var sugarDelta = normalizedDelta(
        BlendProfileAxis.SUGAR,
        point.sugar(),
        nearestTargetPoint.sugar()
    );
    final var acidityDelta = normalizedDelta(
        BlendProfileAxis.ACIDITY,
        point.acidity(),
        nearestTargetPoint.acidity()
    );
    final var weightedSugarDistance = weights.sugar().multiply(sugarDelta.multiply(sugarDelta, MATH_CONTEXT), MATH_CONTEXT);
    final var weightedAcidityDistance = weights.acidity().multiply(acidityDelta.multiply(acidityDelta, MATH_CONTEXT), MATH_CONTEXT);
    final var weightedTanninDistance = weightedTanninDistance(tannin, nearestTargetTannin);
    return weightedSugarDistance
        .add(weightedAcidityDistance, MATH_CONTEXT)
        .add(weightedTanninDistance, MATH_CONTEXT)
        .sqrt(MATH_CONTEXT)
        .stripTrailingZeros();
  }

  private @NonNull BigDecimal weightedTanninDistance(
      @Nullable BigDecimal tannin,
      @Nullable BigDecimal nearestTargetTannin
  ) {
    if (tannin == null || nearestTargetTannin == null) {
      return BigDecimal.ZERO;
    }
    final var tanninDelta = normalizedDelta(
        BlendProfileAxis.TANNIN,
        tannin,
        nearestTargetTannin
    );
    return weights.tanninOrDefault().multiply(tanninDelta.multiply(tanninDelta, MATH_CONTEXT), MATH_CONTEXT);
  }

  private @NonNull BigDecimal normalizedDelta(
      @NonNull BlendProfileAxis axis,
      @NonNull BigDecimal value,
      @NonNull BigDecimal targetValue
  ) {
    return normalizer.normalize(axis, value)
        .subtract(normalizer.normalize(axis, targetValue))
        .abs();
  }
}
