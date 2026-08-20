package org.craft.mcs.cap.core.production.batch.application.usecase;

import org.craft.mcs.cap.core.production.batch.application.contract.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatch;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps a production aggregate to the immutable application read boundary. */
public final class ProductionBatchViewMapper {

  private ProductionBatchViewMapper() {
  }

  public static @NonNull ProductionBatchView toView(@NonNull ProductionBatch batch) {
    requireNonNull(batch, "production batch cannot be null");
    return new ProductionBatchView(
        batch.batchId(),
        batch.workspaceId(),
        batch.name(),
        batch.vesselId(),
        batch.lifecycle(),
        batch.alcoholicFermentationState(),
        batch.currentVolume(),
        batch.currentMaterialStateId(),
        batch.initialProfileSnapshot(),
        batch.note()
    );
  }
}
