package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record BlendProfileTargetDistance(
    @NonNull BlendProfileTargetDistanceStatus status,
    @NonNull SugarAcidityCoordinates point,
    @NonNull SugarAcidityCoordinates nearestTargetPoint,
    @Nullable BigDecimal tannin,
    @Nullable BigDecimal nearestTargetTannin,
    @Nullable BigDecimal value,
    boolean insideSugarAcidityTarget,
    boolean insideTarget,
    @NonNull BlendProfileObjectiveFunctionCode objectiveFunction,
    @NonNull BlendProfileDistanceMetric metric,
    @NonNull BlendProfileObjectiveWeights weights,
    @NonNull SugarAcidityTargetKind targetKind,
    @NonNull SugarAcidityTargetGeometryShape targetShape,
    @NonNull List<@NonNull BlendProfileAxis> missingMeasurements
) {

  public BlendProfileTargetDistance {
    requireNonNull(status, "target distance status cannot be null");
    requireNonNull(point, "sugar-acidity point cannot be null");
    requireNonNull(nearestTargetPoint, "nearest target point cannot be null");
    requireNonNull(objectiveFunction, "objective function cannot be null");
    requireNonNull(metric, "distance metric cannot be null");
    requireNonNull(weights, "objective weights cannot be null");
    requireNonNull(targetKind, "target kind cannot be null");
    requireNonNull(targetShape, "target shape cannot be null");
    missingMeasurements = List.copyOf(requireNonNull(missingMeasurements, "missing measurements cannot be null"));
    if (status == BlendProfileTargetDistanceStatus.SCORED && value == null) {
      throw new IllegalArgumentException("scored target distance value cannot be null");
    }
    if (status == BlendProfileTargetDistanceStatus.UNSCORABLE && missingMeasurements.isEmpty()) {
      throw new IllegalArgumentException("unscorable target distance must contain missing measurements");
    }
    if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("target distance value cannot be negative");
    }
  }

  public boolean scored() {
    return status == BlendProfileTargetDistanceStatus.SCORED;
  }
}
