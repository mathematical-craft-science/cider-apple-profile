package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


/**
 * Maps a user-facing scoring purpose to a default normalizer.
 *
 * <p>REST requests may still override the normalizer explicitly. This policy defines defaults for
 * cases where the caller only knows the business purpose, such as graph exploration or blending
 * planning.
 */
public interface ProfileTargetScoringPurposeNormalizerPolicy {

  @NonNull
  ProfileAxisNormalizerCode normalizerFor(@NonNull ProfileTargetScoringPurpose purpose);
}
