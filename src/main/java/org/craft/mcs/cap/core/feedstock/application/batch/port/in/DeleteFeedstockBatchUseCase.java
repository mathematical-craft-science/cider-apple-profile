package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.jspecify.annotations.NonNull;


public interface DeleteFeedstockBatchUseCase {

  void delete(@NonNull DeleteFeedstockBatchCommand command);
}
