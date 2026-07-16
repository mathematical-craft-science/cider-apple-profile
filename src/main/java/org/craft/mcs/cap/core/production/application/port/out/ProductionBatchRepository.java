package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;

public interface ProductionBatchRepository
    extends VersionedAggregateRepository<ProductionBatch, ProductionBatchId> {
}
