package org.craft.mcs.cap.core.cideranalysis.readiness.domain;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FermentationStartReadinessAssessment(
    @NonNull FermentationStartReadinessStatus status,
    @NonNull String method,
    @NonNull List<@NonNull FermentationStartReadinessCheck> checks
) {

  public FermentationStartReadinessAssessment {
    requireNonNull(status, "fermentation start readiness status cannot be null");
    requireNonNull(method, "fermentation start readiness method cannot be null");
    checks = List.copyOf(requireNonNull(checks, "fermentation start readiness checks cannot be null"));
    if (method.isBlank()) {
      throw new DomainRuleViolationException("fermentation start readiness method cannot be blank");
    }
    final var factors = checks.stream()
        .map(FermentationStartReadinessCheck::factor)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
    if (factors.size() != checks.size() || !factors.equals(Set.of(FermentationStartReadinessFactor.values()))) {
      throw new DomainRuleViolationException(
          "fermentation start readiness requires exactly one check for every factor"
      );
    }
    if (status != statusOf(checks)) {
      throw new DomainRuleViolationException("fermentation start readiness status must match check severities");
    }
  }

  private static @NonNull FermentationStartReadinessStatus statusOf(
      @NonNull List<@NonNull FermentationStartReadinessCheck> checks
  ) {
    if (checks.stream().anyMatch(FermentationStartReadinessAssessment::isBlocker)) {
      return FermentationStartReadinessStatus.NOT_READY;
    }
    if (checks.stream().anyMatch(FermentationStartReadinessAssessment::isWarning)) {
      return FermentationStartReadinessStatus.NEEDS_ATTENTION;
    }
    return FermentationStartReadinessStatus.READY;
  }

  private static boolean isBlocker(@NonNull FermentationStartReadinessCheck check) {
    return check.severity() == FermentationStartReadinessSeverity.BLOCKER;
  }

  private static boolean isWarning(@NonNull FermentationStartReadinessCheck check) {
    return check.severity() == FermentationStartReadinessSeverity.WARNING;
  }
}
