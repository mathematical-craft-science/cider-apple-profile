package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Maps user-facing scoring purposes to built-in normalization strategies.
 */
@Component
public final class BuiltInBlendProfileScoringPurposeNormalizerPolicy
    implements BlendProfileScoringPurposeNormalizerPolicy {

  @Override
  public @NonNull BlendProfileAxisNormalizerCode normalizerFor(
      @NonNull BlendProfileScoringPurpose purpose
  ) {
    requireNonNull(purpose, "blend profile scoring purpose cannot be null");
    return switch (purpose) {
      case GRAPH_EXPLORATION -> BlendProfileAxisNormalizerCode.MIN_MAX;
      case BLENDING_PLANNING -> BlendProfileAxisNormalizerCode.EFFECTIVE_RANGE;
      case TARGET_ZONE_MATCHING -> BlendProfileAxisNormalizerCode.THRESHOLD_SEGMENT;
      case EXPERT_CALIBRATION -> BlendProfileAxisNormalizerCode.SENSITIVITY_UNIT;
    };
  }
}
