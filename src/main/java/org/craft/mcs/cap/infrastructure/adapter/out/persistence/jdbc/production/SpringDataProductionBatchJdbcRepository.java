package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity.ProductionBatchJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


interface SpringDataProductionBatchJdbcRepository
    extends ListCrudRepository<ProductionBatchJdbcEntity, @NonNull UUID> {

  @NonNull
  List<@NonNull ProductionBatchJdbcEntity> findAllByWorkspaceIdOrderByCreatedAtDescBatchIdDesc(
      @NonNull UUID workspaceId
  );

  @NonNull
  List<@NonNull ProductionBatchJdbcEntity> findAllByBatchIdInAndWorkspaceId(
      @NonNull List<@NonNull UUID> batchIds,
      @NonNull UUID workspaceId
  );
}
