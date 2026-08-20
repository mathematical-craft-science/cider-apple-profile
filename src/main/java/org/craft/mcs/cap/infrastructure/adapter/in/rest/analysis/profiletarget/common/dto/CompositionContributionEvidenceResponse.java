package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Per-axis evidence explaining a source contribution role relative to a selected target.")
public record CompositionContributionEvidenceResponse(
    @Schema(
        description = "Evaluated profile axis.",
        implementation = ProfileEvaluationAxis.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileEvaluationAxis axis,
    @Schema(description = "Source axis value in canonical units.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse sourceValue,
    @Schema(description = "Nearest target coordinate used for comparison.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse targetCoordinate,
    @Schema(
        description = "Signed numeric difference from the target closure coordinate. It can be zero at an "
            + "excluded upper boundary while direction remains ABOVE_TARGET.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse deviationFromTarget,
    @Schema(
        description = "Membership-aware direction relative to the selected target, including exclusive bounds.",
        implementation = CompositionContributionEvidenceDirection.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionContributionEvidenceDirection direction,
    @Schema(
        description = "Cider-making effect inferred from this target-aware evidence.",
        implementation = CompositionContributionEvidenceEffect.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionContributionEvidenceEffect effect,
    @Schema(
        description = "Strength of this evidence within the complete contribution-role assessment.",
        implementation = CompositionContributionEvidenceSeverity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionContributionEvidenceSeverity severity
) {
}
