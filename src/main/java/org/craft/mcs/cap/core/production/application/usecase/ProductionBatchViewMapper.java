package org.craft.mcs.cap.core.production.application.usecase;

import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps a production aggregate to the immutable application read boundary. */
final class ProductionBatchViewMapper {

  private ProductionBatchViewMapper() {
  }

  static @NonNull ProductionBatchView toView(@NonNull ProductionBatch batch) {
    requireNonNull(batch, "production batch cannot be null");
    return new ProductionBatchView(
        batch.batchId(),
        batch.workspaceId(),
        batch.name(),
        batch.vessel(),
        batch.lifecycle(),
        batch.alcoholicFermentationState(),
        batch.currentVolume(),
        batch.currentMaterialStateId(),
        batch.initialProfileSnapshot(),
        batch.note()
    );
  }
}
