package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Single fermentation-start readiness check.")
public record FermentationStartReadinessCheckResponse(
    @Schema(
        description = "Checked factor.",
        implementation = Factor.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Factor factor,
    @Schema(
        description = "Check severity.",
        implementation = Severity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Severity severity,
    @Schema(
        description = "Stable machine-readable check code.",
        implementation = Code.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Code code,
    @Schema(description = "Human-readable check message.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message,
    @Schema(
        description = "Measured or derived quantity used by the check.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse value,
    @Schema(
        description = "Corrective action that currently explains a managed risk.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String managedByAction
) {

  public enum Factor {
    SPECIFIC_GRAVITY,
    PH,
    YEAST_ASSIMILABLE_NITROGEN
  }

  public enum Severity {
    INFO,
    WARNING,
    BLOCKER
  }

  public enum Code {
    SPECIFIC_GRAVITY_MISSING,
    SPECIFIC_GRAVITY_PRESENT,
    PH_MISSING,
    PH_BELOW_GENERIC_YEAST_TOLERANCE,
    PH_LOW,
    PH_VERY_HIGH_MICROBIAL_RISK,
    PH_VERY_HIGH_MICROBIAL_RISK_MANAGED,
    PH_HIGH_MICROBIAL_RISK,
    PH_HIGH_MICROBIAL_RISK_MANAGED,
    PH_ACCEPTABLE,
    YAN_MISSING,
    YAN_SEVERELY_LOW,
    YAN_SEVERELY_LOW_MANAGED,
    YAN_LOW,
    YAN_LOW_MANAGED,
    YAN_HIGH,
    YAN_ACCEPTABLE
  }
}
