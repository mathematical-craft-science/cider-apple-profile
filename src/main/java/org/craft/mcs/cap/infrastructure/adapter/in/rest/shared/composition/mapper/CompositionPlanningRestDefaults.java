package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;


public final class CompositionPlanningRestDefaults {

  private static final @NonNull ProfileAxisNormalizerCode PROFILE_TARGET_NORMALIZER =
      ProfileAxisNormalizerCode.SENSITIVITY_UNIT;
  private static final @NonNull ProfileTargetObjectiveFunctionCode PROFILE_TARGET_OBJECTIVE_FUNCTION =
      ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA;

  private CompositionPlanningRestDefaults() {
  }

  public static @NonNull ProfileAxisNormalizerCode profileTargetNormalizer() {
    return PROFILE_TARGET_NORMALIZER;
  }

  public static @NonNull ProfileTargetObjectiveFunctionCode profileTargetObjectiveFunction() {
    return PROFILE_TARGET_OBJECTIVE_FUNCTION;
  }
}
