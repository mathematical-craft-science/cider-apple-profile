package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Chemical axis value captured in a trace snapshot.")
public record BatchTraceAxisValueResponse(
    @Schema(description = "Chemical axis.", implementation = ChemicalAxis.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalAxis axis,
    @Schema(
        description = "Axis value in CAP canonical units: sugar as SG, acidity as g/L malic acid, tanninity as mg/L TAE, YAN as mg N/L.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal value,
    @Schema(description = "How the axis value was obtained.", implementation = MeasurementValueKind.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementValueKind valueKind,
    @Schema(description = "Range minimum, when value was derived from a range.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal min,
    @Schema(description = "Range maximum, when value was derived from a range.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal max,
    @Schema(description = "Standard deviation, when value was derived from statistical data.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal standardDeviation,
    @Schema(description = "Sample size, when value was derived from statistical data.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Integer sampleSize
) {
}
