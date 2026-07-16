package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import static java.util.Objects.requireNonNull;

/** Authoritative product interpretation of a technical profile-target distance. */
public record ProfileTargetFitAssessment(
    @Nullable Integer matchPercent,
    @NonNull ProfileTargetFitStatus status
) {

  public ProfileTargetFitAssessment {
    requireNonNull(status, "profile target fit status cannot be null");
    if (matchPercent != null && (matchPercent < 0 || matchPercent > 100)) {
      throw new DomainRuleViolationException("profile target match percent must be between 0 and 100");
    }
    if ((status == ProfileTargetFitStatus.UNSCORABLE) != (matchPercent == null)) {
      throw new DomainRuleViolationException("unscorable profile target fit must not have a match percent");
    }
    if (matchPercent != null && status != ProfileTargetFitPolicy.statusFor(matchPercent)) {
      throw new DomainRuleViolationException("profile target fit status does not match its percentage");
    }
  }

  public static @NonNull ProfileTargetFitAssessment scored(int matchPercent) {
    return new ProfileTargetFitAssessment(matchPercent, ProfileTargetFitPolicy.statusFor(matchPercent));
  }

  public static @NonNull ProfileTargetFitAssessment unscorable() {
    return new ProfileTargetFitAssessment(null, ProfileTargetFitStatus.UNSCORABLE);
  }
}
