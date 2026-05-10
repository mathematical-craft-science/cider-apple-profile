package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;


@Schema(description = "Chemical profile capability assessment.")
public record ChemicalProfileCapabilityAssessmentResponse(
    @Schema(description = "Capability code.", implementation = ChemicalProfileCapability.class, example = "SUGAR_ACIDITY_PLOT")
    @NonNull ChemicalProfileCapability capability,
    @Schema(description = "Whether the chemical profile has all measurements required by this capability.", example = "true")
    boolean capable,
    @ArraySchema(schema = @Schema(description = "Measurement axes required by the capability.", implementation = ChemicalAxis.class))
    @NonNull List<@NonNull ChemicalAxis> requiredAxes,
    @ArraySchema(schema = @Schema(description = "Measurement axes present in the profile.", implementation = ChemicalAxis.class))
    @NonNull List<@NonNull ChemicalAxis> presentAxes,
    @ArraySchema(schema = @Schema(description = "Measurement axes missing from the profile.", implementation = ChemicalAxis.class))
    @NonNull List<@NonNull ChemicalAxis> missingAxes
) {
}
