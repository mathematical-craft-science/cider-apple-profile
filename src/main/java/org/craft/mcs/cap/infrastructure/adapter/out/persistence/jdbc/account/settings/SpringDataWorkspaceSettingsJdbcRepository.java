package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.settings;

import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data command repository for WorkspaceSettings JDBC entities. */
interface SpringDataWorkspaceSettingsJdbcRepository
    extends ListCrudRepository<WorkspaceSettingsJdbcEntity, @NonNull UUID> {
}
