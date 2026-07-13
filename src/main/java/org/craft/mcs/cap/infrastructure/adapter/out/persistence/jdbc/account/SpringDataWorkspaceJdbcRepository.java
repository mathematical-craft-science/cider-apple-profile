package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.WorkspaceJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data command repository for Workspace JDBC entities. */
interface SpringDataWorkspaceJdbcRepository
    extends ListCrudRepository<WorkspaceJdbcEntity, @NonNull UUID> {

  @NonNull Optional<@NonNull WorkspaceJdbcEntity> findByCiderMakerId(@NonNull UUID ciderMakerId);
}
