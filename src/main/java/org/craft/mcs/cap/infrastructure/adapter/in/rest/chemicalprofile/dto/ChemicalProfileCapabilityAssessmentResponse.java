package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;


@Schema(description = "Chemical profile capability assessment.")
public record ChemicalProfileCapabilityAssessmentResponse(
    @Schema(description = "Capability code.", implementation = ChemicalProfileCapability.class, example = "SUGAR_ACIDITY_PLOT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalProfileCapability capability,
    @Schema(description = "Whether the chemical profile has all measurements required by this capability.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean capable,
    @ArraySchema(
        arraySchema = @Schema(description = "Measurement axes required by the capability.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalAxis.class)
    )
    @NonNull List<@NonNull ChemicalAxis> requiredAxes,
    @ArraySchema(
        arraySchema = @Schema(description = "Measurement axes present in the profile.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalAxis.class)
    )
    @NonNull List<@NonNull ChemicalAxis> presentAxes,
    @ArraySchema(
        arraySchema = @Schema(description = "Measurement axes missing from the profile.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalAxis.class)
    )
    @NonNull List<@NonNull ChemicalAxis> missingAxes
) {
}
