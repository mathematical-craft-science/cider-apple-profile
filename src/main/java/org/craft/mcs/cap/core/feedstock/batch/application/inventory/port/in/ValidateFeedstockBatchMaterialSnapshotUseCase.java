package org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in;

import org.jspecify.annotations.NonNull;


/** Atomically verifies one inspected Feedstock snapshot under its aggregate row lock. */
public interface ValidateFeedstockBatchMaterialSnapshotUseCase {

  void validate(@NonNull ValidateFeedstockBatchMaterialSnapshotCommand command);
}
