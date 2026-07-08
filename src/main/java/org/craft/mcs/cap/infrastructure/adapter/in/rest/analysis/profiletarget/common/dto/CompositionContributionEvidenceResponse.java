package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRole;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Per-axis evidence explaining a source contribution role relative to a selected target.")
public record CompositionContributionEvidenceResponse(
    @Schema(description = "Evaluated profile axis.", implementation = ProfileEvaluationAxis.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileEvaluationAxis axis,
    @Schema(description = "Source axis value in canonical units.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse sourceValue,
    @Schema(description = "Nearest target coordinate used for comparison.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse targetCoordinate,
    @Schema(description = "Source value minus target coordinate. Positive means above target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse deviationFromTarget,
    @Schema(description = "Role explained by this axis.", implementation = CompositionContributionRole.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionContributionRole role
) {
}
