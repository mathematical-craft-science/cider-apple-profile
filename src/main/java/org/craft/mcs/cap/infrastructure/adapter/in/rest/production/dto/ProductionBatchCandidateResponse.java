package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Saved production candidate with its authoritative composition assessment.")
public record ProductionBatchCandidateResponse(
    @Schema(
        description = "Production candidate identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID candidateId,
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Candidate lifecycle status.",
        implementation = ProductionBatchCandidateStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProductionBatchCandidateStatus status,
    @Schema(
        description = "Authoritative source composition and target assessment.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProductionBatchCompositionResponse composition,
    @Schema(
        description = "Production batch created from this candidate, when confirmed.",
        format = "uuid",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable UUID confirmedProductionBatchId
) {
}
