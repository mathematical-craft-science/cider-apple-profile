package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.Normalizer;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyProfileTargetCandidates(
    @NonNull AnalysisScenario analysisScenario,
    @NonNull Normalizer normalizerCode,
    @NonNull List<@NonNull AppleVarietyProfileTargetCandidate> candidates
) {

  public AppleVarietyProfileTargetCandidates {
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "apple variety profile target candidates cannot be null"));
  }
}
