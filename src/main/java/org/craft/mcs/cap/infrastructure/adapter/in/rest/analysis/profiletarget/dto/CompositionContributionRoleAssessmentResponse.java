package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionRole;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware role of a profile source in a possible composition.")
public record CompositionContributionRoleAssessmentResponse(
    @ArraySchema(schema = @Schema(description = "All roles assigned to the source.", implementation = CompositionContributionRole.class))
    @NonNull List<@NonNull CompositionContributionRole> roles,
    @Schema(description = "Primary role shown first to the user.", implementation = CompositionContributionRole.class)
    @NonNull CompositionContributionRole primaryRole,
    @ArraySchema(schema = @Schema(description = "Per-axis explanation for assigned roles.", implementation = CompositionContributionEvidenceResponse.class))
    @NonNull List<@NonNull CompositionContributionEvidenceResponse> evidence,
    @ArraySchema(schema = @Schema(description = "Axes missing for role assessment.", implementation = ChemicalAxis.class))
    @NonNull List<@NonNull ChemicalAxis> missingAxes,
    @Schema(description = "Whether contribution role could be assessed.", example = "true")
    boolean assessable
) {
}
