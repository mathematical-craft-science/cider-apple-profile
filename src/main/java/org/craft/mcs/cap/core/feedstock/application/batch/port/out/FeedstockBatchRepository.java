package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;

public interface FeedstockBatchRepository
    extends VersionedAggregateRepository<FeedstockBatch, FeedstockBatchId> {
}
