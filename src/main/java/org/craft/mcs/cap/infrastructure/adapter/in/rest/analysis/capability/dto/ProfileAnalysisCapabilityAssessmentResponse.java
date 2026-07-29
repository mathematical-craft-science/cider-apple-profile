package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.capability.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;


@Schema(description = "Availability assessment for the inputs required by a profile-analysis scenario.")
public record ProfileAnalysisCapabilityAssessmentResponse(
    @Schema(
        description = "Capability code.",
        implementation = ProfileAnalysisCapability.class,
        example = "SUGAR_ACIDITY_GRAPH",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileAnalysisCapability capability,
    @Schema(
        description = "Whether all canonical characteristics required by this capability are available.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean capable,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Material characteristics required by the capability.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementCharacteristic.class)
    )
    @NonNull List<@NonNull MeasurementCharacteristic> requiredCharacteristics,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Required characteristics that are present in the evaluated input.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementCharacteristic.class)
    )
    @NonNull List<@NonNull MeasurementCharacteristic> presentCharacteristics,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Required characteristics that are missing from the evaluated input.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementCharacteristic.class)
    )
    @NonNull List<@NonNull MeasurementCharacteristic> missingCharacteristics
) {
}
