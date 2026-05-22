package org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FermentationStartReadinessCheck(
    @NonNull FermentationStartReadinessFactor factor,
    @NonNull FermentationStartReadinessSeverity severity,
    @NonNull String code,
    @NonNull String message,
    @Nullable BigDecimal value,
    @Nullable String unit
) {

  public FermentationStartReadinessCheck {
    requireNonNull(factor, "fermentation start readiness factor cannot be null");
    requireNonNull(severity, "fermentation start readiness severity cannot be null");
    requireNonNull(code, "fermentation start readiness check code cannot be null");
    requireNonNull(message, "fermentation start readiness check message cannot be null");
  }
}
