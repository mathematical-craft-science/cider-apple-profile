package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.audit;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.out.WorkspaceAdministrativeAuditRepository;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditEntry;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;

/** Appends and reads immutable workspace administration audit facts. */
@Repository
@RequiredArgsConstructor
class JdbcWorkspaceAdministrativeAuditRepository implements WorkspaceAdministrativeAuditRepository {

  private final @NonNull SpringDataWorkspaceAdministrativeAuditJdbcRepository repository;

  @Override
  public void append(@NonNull WorkspaceAdministrativeAuditEntry entry) {
    requireNonNull(entry, "workspace administrative audit entry cannot be null");
    repository.save(WorkspaceAuditJdbcMapper.fromDomain(entry));
  }

  @Override
  public @NonNull List<@NonNull WorkspaceAdministrativeAuditEntry> findAllByWorkspaceId(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findAllByWorkspaceIdOrderByOccurredAtDescAuditEntryIdDesc(
        workspaceReference(workspaceId)
    ).stream().map(WorkspaceAuditJdbcMapper::toDomain).toList();
  }

  private static @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceReference(
      @NonNull WorkspaceId workspaceId
  ) {
    return AggregateReference.to(workspaceId.value());
  }
}
