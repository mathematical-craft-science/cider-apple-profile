package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Maps the Workspace aggregate to its Spring Data JDBC representation. */
final class WorkspaceJdbcMapper {

  private WorkspaceJdbcMapper() {
  }

  static @NonNull WorkspaceJdbcEntity fromDomain(@NonNull Workspace workspace) {
    return new WorkspaceJdbcEntity(
        workspace.workspaceId().value(),
        workspace.name().value(),
        workspace.type(),
        workspace.status(),
        workspace.externalOrganizationProvider() == null
            ? null
            : workspace.externalOrganizationProvider().value(),
        workspace.externalOrganizationSubject() == null
            ? null
            : workspace.externalOrganizationSubject().value()
    );
  }

  static @NonNull Workspace toDomain(@NonNull WorkspaceJdbcEntity entity) {
    return new Workspace(
        WorkspaceId.of(entity.workspaceId),
        DisplayName.of(entity.name),
        entity.type,
        entity.status,
        entity.externalOrganizationProvider == null
            ? null
            : IdentityProviderCode.of(entity.externalOrganizationProvider),
        entity.externalOrganizationSubject == null
            ? null
            : ExternalOrganizationSubject.of(entity.externalOrganizationSubject)
    );
  }

  static @NonNull VersionedAggregate<Workspace> toVersionedAggregate(
      @NonNull WorkspaceJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

}
