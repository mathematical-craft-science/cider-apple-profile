package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Maps user-facing scoring purposes to built-in normalization strategies.
 */
@Component
public final class BuiltInProfileTargetScoringPurposeNormalizerPolicy
    implements ProfileTargetScoringPurposeNormalizerPolicy {

  @Override
  public @NonNull ProfileAxisNormalizerCode normalizerFor(
      @NonNull ProfileTargetScoringPurpose purpose
  ) {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    return switch (purpose) {
      case GRAPH_EXPLORATION -> ProfileAxisNormalizerCode.MIN_MAX;
      case BLENDING_PLANNING -> ProfileAxisNormalizerCode.EFFECTIVE_RANGE;
      case TARGET_ZONE_MATCHING -> ProfileAxisNormalizerCode.THRESHOLD_SEGMENT;
      case EXPERT_CALIBRATION -> ProfileAxisNormalizerCode.SENSITIVITY_UNIT;
    };
  }
}
