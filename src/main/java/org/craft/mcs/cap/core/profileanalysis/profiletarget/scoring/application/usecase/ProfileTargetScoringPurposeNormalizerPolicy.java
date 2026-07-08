package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
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
