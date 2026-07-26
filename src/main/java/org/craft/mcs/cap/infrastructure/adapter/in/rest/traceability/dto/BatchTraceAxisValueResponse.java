package org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Chemical axis value captured in a trace snapshot.")
public record BatchTraceAxisValueResponse(
    @Schema(
        description = "Chemical axis.",
        implementation = ChemicalAxis.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ChemicalAxis axis,
    @Schema(
        description = "Axis value in CAP canonical units: sugar as SG, acidity as g/L malic acid, "
            + "tannin as mg/L TAE, YAN as mg N/L.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse value,
    @Schema(
        description = "How the axis value was obtained.",
        implementation = AxisValueKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AxisValueKind valueKind,
    @Schema(
        description = "Interval minimum.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse min,
    @Schema(
        description = "Interval maximum.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse max
) {
}
