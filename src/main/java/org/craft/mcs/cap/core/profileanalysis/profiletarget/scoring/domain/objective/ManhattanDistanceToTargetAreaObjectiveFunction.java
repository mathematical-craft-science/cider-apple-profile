package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizer;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTarget;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import static java.util.Objects.requireNonNull;


/**
 * Scores a point by normalized weighted Manhattan (L1) distance to the nearest target area boundary.
 *
 * <pre>{@code
 * distance =
 *   sugarWeight   * abs(normalizedSugarDelta)
 * + acidityWeight * abs(normalizedAcidityDelta)
 * + tanninWeight  * abs(normalizedTanninDelta)
 * }</pre>
 *
 * L1 is useful for optimization scenarios where each axis deviation should add linearly and remain
 * easier to explain than squared-error distance. It may also be a better first operational metric
 * for blending recommendations where a cider maker wants to see additive trade-offs by axis.
 */
public final class ManhattanDistanceToTargetAreaObjectiveFunction implements ProfileTargetObjectiveFunction {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull ProfileAxisNormalizer normalizer;
  private final @NonNull ProfileTargetObjectiveWeights weights;

  public ManhattanDistanceToTargetAreaObjectiveFunction(
      @NonNull ProfileAxisNormalizer normalizer,
      @NonNull ProfileTargetObjectiveWeights weights
  ) {
    this.normalizer = requireNonNull(normalizer, "profile axis normalizer cannot be null");
    this.weights = requireNonNull(weights, "profile target objective weights cannot be null");
  }

  @Override
  public @NonNull ProfileTargetObjectiveFunctionCode code() {
    return ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA;
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
    requireNonNull(objective, "profile target objective cannot be null");
    return score(
        SugarAcidityCoordinates.from(point),
        tanninValue(point),
        objective
    );
  }

  private static @Nullable BigDecimal tanninValue(@NonNull SugarAcidityProfilePoint point) {
    final var tannin = point.tannin();
    return tannin == null ? null : tannin.value();
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
          ProfileDistanceMetric.NORMALIZED_WEIGHTED_MANHATTAN,
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
        ProfileDistanceMetric.NORMALIZED_WEIGHTED_MANHATTAN,
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
    final var sugarDistance = weightedDelta(
        ProfileEvaluationAxis.SUGAR,
        point.sugar(),
        nearestTargetPoint.sugar(),
        weights.sugar()
    );
    final var acidityDistance = weightedDelta(
        ProfileEvaluationAxis.ACIDITY,
        point.acidity(),
        nearestTargetPoint.acidity(),
        weights.acidity()
    );
    final var tanninDistance = weightedTanninDistance(tannin, nearestTargetTannin);
    return sugarDistance
        .add(acidityDistance, MATH_CONTEXT)
        .add(tanninDistance, MATH_CONTEXT)
        .stripTrailingZeros();
  }

  private @NonNull BigDecimal weightedTanninDistance(
      @Nullable BigDecimal tannin,
      @Nullable BigDecimal nearestTargetTannin
  ) {
    if (tannin == null || nearestTargetTannin == null) {
      return BigDecimal.ZERO;
    }
    return weightedDelta(ProfileEvaluationAxis.TANNIN, tannin, nearestTargetTannin, weights.tanninOrDefault());
  }

  private @NonNull BigDecimal weightedDelta(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value,
      @NonNull BigDecimal targetValue,
      @NonNull BigDecimal weight
  ) {
    return weight.multiply(
        normalizer.normalize(axis, value)
            .subtract(normalizer.normalize(axis, targetValue))
            .abs(),
        MATH_CONTEXT
    );
  }
}
