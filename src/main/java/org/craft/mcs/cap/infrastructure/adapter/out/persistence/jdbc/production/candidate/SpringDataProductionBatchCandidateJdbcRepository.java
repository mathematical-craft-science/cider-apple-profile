package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate;

import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


/** Generated CRUD repository for the production-candidate aggregate table. */
interface SpringDataProductionBatchCandidateJdbcRepository
    extends ListCrudRepository<ProductionBatchCandidateJdbcEntity, @NonNull UUID> {
}
