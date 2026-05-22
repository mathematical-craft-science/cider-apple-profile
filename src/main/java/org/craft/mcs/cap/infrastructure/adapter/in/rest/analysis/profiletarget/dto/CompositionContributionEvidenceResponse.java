package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionRole;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.jspecify.annotations.NonNull;


@Schema(description = "Per-axis evidence explaining a source contribution role relative to a selected target.")
public record CompositionContributionEvidenceResponse(
    @Schema(description = "Evaluated profile axis.", implementation = ProfileEvaluationAxis.class)
    @NonNull ProfileEvaluationAxis axis,
    @Schema(description = "Source axis value in canonical units.")
    @NonNull BigDecimal sourceValue,
    @Schema(description = "Nearest target coordinate used for comparison.")
    @NonNull BigDecimal targetCoordinate,
    @Schema(description = "Source value minus target coordinate. Positive means above target.")
    @NonNull BigDecimal deviationFromTarget,
    @Schema(description = "Role explained by this axis.", implementation = CompositionContributionRole.class)
    @NonNull CompositionContributionRole role
) {
}
