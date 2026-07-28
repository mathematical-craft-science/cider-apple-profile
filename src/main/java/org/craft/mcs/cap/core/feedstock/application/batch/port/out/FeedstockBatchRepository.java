package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;

public interface FeedstockBatchRepository
    extends VersionedAggregateRepository<FeedstockBatch, FeedstockBatchId> {

  void delete(@NonNull VersionedAggregate<FeedstockBatch> feedstockBatch);
}
