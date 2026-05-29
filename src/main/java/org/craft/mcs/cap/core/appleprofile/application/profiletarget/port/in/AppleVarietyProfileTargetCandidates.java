package org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyProfileTargetCandidates(
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull List<@NonNull AppleVarietyProfileTargetCandidate> candidates
) {

  public AppleVarietyProfileTargetCandidates {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "apple variety profile target candidates cannot be null"));
  }
}
