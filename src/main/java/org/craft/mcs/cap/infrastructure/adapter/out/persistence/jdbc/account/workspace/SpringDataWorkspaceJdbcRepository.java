package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace;

import java.util.UUID;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Spring Data command repository for Workspace JDBC entities. */
interface SpringDataWorkspaceJdbcRepository
    extends ListCrudRepository<WorkspaceJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE workspaces
         SET name = :name,
             type = :type,
             status = :status,
             external_organization_provider = :externalOrganizationProvider,
             external_organization_subject = :externalOrganizationSubject,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE workspace_id = :workspaceId
         AND version = :expectedVersion
      """)
  int updateByIdAndVersion(
      @Param("workspaceId") @NonNull UUID workspaceId,
      @Param("name") @NonNull String name,
      @Param("type") @NonNull WorkspaceType type,
      @Param("status") @NonNull WorkspaceStatus status,
      @Param("externalOrganizationProvider") @Nullable String externalOrganizationProvider,
      @Param("externalOrganizationSubject") @Nullable String externalOrganizationSubject,
      @Param("expectedVersion") long expectedVersion
  );
}
