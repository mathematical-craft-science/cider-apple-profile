package org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain;

import java.util.List;
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
  }
}
