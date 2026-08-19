package org.craft.mcs.cap.core.feedstock.batch.application.detail.port.in;

import org.craft.mcs.cap.core.feedstock.batch.application.detail.model.FeedstockBatchDetailView;
import org.jspecify.annotations.NonNull;


/**
 * Inbound application port for reading one feedstock batch detail view.
 *
 * <p>The use case exposes a stable aggregate-oriented read model for details pages and API clients.
 */
public interface GetFeedstockBatchDetailUseCase {

  @NonNull
  FeedstockBatchDetailView get(@NonNull GetFeedstockBatchDetailQuery query);
}
