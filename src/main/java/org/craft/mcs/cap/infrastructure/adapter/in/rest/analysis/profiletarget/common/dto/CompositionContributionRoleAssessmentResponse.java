package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionRole;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware role of a profile source in a possible composition.")
public record CompositionContributionRoleAssessmentResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "All roles assigned to the source.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionContributionRole.class)
    )
    @NonNull List<@NonNull CompositionContributionRole> roles,
    @Schema(description = "Primary role shown first to the user.", implementation = CompositionContributionRole.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionContributionRole primaryRole,
    @ArraySchema(
        arraySchema = @Schema(description = "Per-axis explanation for assigned roles.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionContributionEvidenceResponse.class)
    )
    @NonNull List<@NonNull CompositionContributionEvidenceResponse> evidence,
    @ArraySchema(
        arraySchema = @Schema(description = "Axes missing for role assessment.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalAxis.class)
    )
    @NonNull List<@NonNull ChemicalAxis> missingAxes,
    @Schema(description = "Whether contribution role could be assessed.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean assessable
) {
}
