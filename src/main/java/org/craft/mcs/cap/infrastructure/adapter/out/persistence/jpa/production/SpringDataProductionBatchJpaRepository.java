package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.production;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.production.entity.ProductionBatchJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;


interface SpringDataProductionBatchJpaRepository
    extends JpaRepository<ProductionBatchJpaEntity, @NonNull UUID> {
}
