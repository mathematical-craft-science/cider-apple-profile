package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTarget;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Scores a profile point against a configured target.
 *
 * <p>An objective function combines target geometry, optional tannin target, axis normalizer and
 * per-axis weights into one {@link ProfileTargetDistance}. It does not decide which normalizer to
 * use; that is done by application policy or request configuration before the objective function is
 * created by {@link ProfileTargetObjectiveFunctionFactory}.
 */
public interface ProfileTargetObjectiveFunction {

  @NonNull
  ProfileTargetObjectiveFunctionCode code();

  @NonNull
  ProfileTargetDistance distanceTo(
      @NonNull SugarAcidityCoordinates point,
      @NonNull SugarAcidityTarget target
  );

  @NonNull
  ProfileTargetDistance distanceTo(
      @NonNull SugarAcidityProfilePoint point,
      @NonNull ProfileTargetObjective objective
  );

  default @NonNull ProfileTargetDistance distanceTo(
      @NonNull SugarAcidityProfilePoint point,
      @NonNull SugarAcidityTarget target
  ) {
    requireNonNull(point, "sugar-acidity profile point cannot be null");
    return distanceTo(point, ProfileTargetObjective.of(target));
  }
}
