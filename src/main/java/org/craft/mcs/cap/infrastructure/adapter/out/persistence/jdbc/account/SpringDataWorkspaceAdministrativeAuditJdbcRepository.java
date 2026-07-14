package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.WorkspaceAuditJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.WorkspaceJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data repository dedicated to immutable workspace administration audit records. */
interface SpringDataWorkspaceAdministrativeAuditJdbcRepository
    extends ListCrudRepository<WorkspaceAuditJdbcEntity, @NonNull UUID> {

  @NonNull List<@NonNull WorkspaceAuditJdbcEntity>
      findAllByWorkspaceIdOrderByOccurredAtDescAuditEntryIdDesc(
          @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId
      );
}
