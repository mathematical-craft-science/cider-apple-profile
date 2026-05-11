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
public final class DistanceToTargetAreaObjectiveFunction implements ProfileTargetObjectiveFunction {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull ProfileAxisNormalizer normalizer;
  private final @NonNull ProfileTargetObjectiveWeights weights;

  public DistanceToTargetAreaObjectiveFunction(
      @NonNull ProfileAxisNormalizer normalizer
  ) {
    this(normalizer, ProfileTargetObjectiveWeights.balanced());
  }

  public DistanceToTargetAreaObjectiveFunction(
      @NonNull ProfileAxisNormalizer normalizer,
      @NonNull ProfileTargetObjectiveWeights weights
  ) {
    this.normalizer = requireNonNull(normalizer, "profile axis normalizer cannot be null");
    this.weights = requireNonNull(weights, "sugar-acidity objective weights cannot be null");
  }

  @Override
  public @NonNull ProfileTargetObjectiveFunctionCode code() {
    return ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA;
  }

  @Override
  public @NonNull ProfileTargetDistance distanceTo(
      @NonNull SugarAcidityCoordinates point,
      @NonNull SugarAcidityTarget target
  ) {
    requireNonNull(point, "sugar-acidity point cannot be null");
    requireNonNull(target, "sugar-acidity target cannot be null");
    return score(point, null, ProfileTargetObjective.of(target));
  }

  @Override
  public @NonNull ProfileTargetDistance distanceTo(
      @NonNull SugarAcidityProfilePoint point,
      @NonNull ProfileTargetObjective objective
  ) {
    requireNonNull(point, "sugar-acidity profile point cannot be null");
    requireNonNull(objective, "sugar-acidity profile objective cannot be null");
    return score(
        SugarAcidityCoordinates.from(point),
        point.tannin() == null ? null : point.tannin().value(), // NOSONAR
        objective
    );
  }

  private @NonNull ProfileTargetDistance score(
      @NonNull SugarAcidityCoordinates point,
      @Nullable BigDecimal tannin,
      @NonNull ProfileTargetObjective objective
  ) {
    final var target = objective.sugarAcidityTarget();
    final var insideSugarAcidityTarget = target.geometry().contains(point);
    final var nearestTargetPoint = insideSugarAcidityTarget ? point : target.geometry().nearestPointTo(point);
    final var tanninTarget = objective.tanninTarget();
    if (tanninTarget != null && tannin == null) {
      return new ProfileTargetDistance(
          ProfileTargetDistanceStatus.UNSCORABLE,
          point,
          nearestTargetPoint,
          null,
          null,
          null,
          insideSugarAcidityTarget,
          false,
          code(),
          ProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
          weights,
          target.kind(),
          target.geometry().shape(),
          List.of(ProfileEvaluationAxis.TANNIN)
      );
    }

    final var nearestTargetTannin = tanninTarget == null ? null : tanninTarget.nearestValueTo(tannin);
    final var insideTanninTarget = tanninTarget == null || tanninTarget.contains(tannin);
    final var distance = normalizedWeightedDistance(point, nearestTargetPoint, tannin, nearestTargetTannin);

    return new ProfileTargetDistance(
        ProfileTargetDistanceStatus.SCORED,
        point,
        nearestTargetPoint,
        tannin,
        nearestTargetTannin,
        distance,
        insideSugarAcidityTarget,
        insideSugarAcidityTarget && insideTanninTarget,
        code(),
        ProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
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
        ProfileEvaluationAxis.SUGAR,
        point.sugar(),
        nearestTargetPoint.sugar()
    );
    final var acidityDelta = normalizedDelta(
        ProfileEvaluationAxis.ACIDITY,
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
        ProfileEvaluationAxis.TANNIN,
        tannin,
        nearestTargetTannin
    );
    return weights.tanninOrDefault().multiply(tanninDelta.multiply(tanninDelta, MATH_CONTEXT), MATH_CONTEXT);
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
