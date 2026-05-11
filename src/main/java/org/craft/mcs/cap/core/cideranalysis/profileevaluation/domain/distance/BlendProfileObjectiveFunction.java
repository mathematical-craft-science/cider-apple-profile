package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTarget;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public interface BlendProfileObjectiveFunction {

  @NonNull
  BlendProfileObjectiveFunctionCode code();

  @NonNull
  BlendProfileTargetDistance distanceTo(
      @NonNull SugarAcidityCoordinates point,
      @NonNull SugarAcidityTarget target
  );

  @NonNull
  BlendProfileTargetDistance distanceTo(
      @NonNull SugarAcidityProfilePoint point,
      @NonNull BlendProfileObjective objective
  );

  default @NonNull BlendProfileTargetDistance distanceTo(
      @NonNull SugarAcidityProfilePoint point,
      @NonNull SugarAcidityTarget target
  ) {
    requireNonNull(point, "sugar-acidity profile point cannot be null");
    return distanceTo(point, BlendProfileObjective.of(target));
  }
}
