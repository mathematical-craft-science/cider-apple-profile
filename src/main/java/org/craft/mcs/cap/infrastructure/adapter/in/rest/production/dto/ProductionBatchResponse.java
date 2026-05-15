package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record ProductionBatchResponse(
    @NonNull UUID batchId,
    @NonNull UUID ciderMakerId,
    @NonNull String name,
    @NonNull ProductionBatchKind kind,
    @NonNull ProductionBatchStatus status,
    @NonNull ProductionBatchCompositionResponse composition,
    @Nullable String note
) {
}
