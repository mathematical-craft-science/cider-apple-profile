package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace;

import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data command repository for Workspace JDBC entities. */
interface SpringDataWorkspaceJdbcRepository
    extends ListCrudRepository<WorkspaceJdbcEntity, @NonNull UUID> {
}
