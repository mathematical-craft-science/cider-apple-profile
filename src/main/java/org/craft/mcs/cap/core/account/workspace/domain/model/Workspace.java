package org.craft.mcs.cap.core.account.workspace.domain.model;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** CAP-owned collaboration and data-ownership boundary. */
@AggregateRoot
public record Workspace(
    @NonNull WorkspaceId workspaceId,
    @NonNull DisplayName name,
    @NonNull WorkspaceType type,
    @NonNull WorkspaceStatus status,
    @Nullable IdentityProviderCode externalOrganizationProvider,
    @Nullable ExternalOrganizationSubject externalOrganizationSubject
) {

  public Workspace {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "workspace name cannot be null");
    requireNonNull(type, "workspace type cannot be null");
    requireNonNull(status, "workspace status cannot be null");
    if ((externalOrganizationProvider == null) != (externalOrganizationSubject == null)) {
      throw new DomainRuleViolationException(
          "external organization provider and subject must be specified together"
      );
    }
  }

  public static @NonNull Workspace createDefault(
      @NonNull DisplayName name,
      @NonNull WorkspaceType type,
      @Nullable IdentityProviderCode externalOrganizationProvider,
      @Nullable ExternalOrganizationSubject externalOrganizationSubject
  ) {
    return new Workspace(
        WorkspaceId.newId(),
        name,
        type,
        WorkspaceStatus.ACTIVE,
        externalOrganizationProvider,
        externalOrganizationSubject
    );
  }

  /** Creates an additional CAP-owned workspace without an external organization binding. */
  public static @NonNull Workspace create(
      @NonNull DisplayName name,
      @NonNull WorkspaceType type
  ) {
    return createDefault(name, type, null, null);
  }

  /** Changes editable workspace metadata while preserving identity and provider binding. */
  public @NonNull Workspace update(
      @NonNull DisplayName updatedName,
      @NonNull WorkspaceType updatedType
  ) {
    requireActive();
    return new Workspace(
        workspaceId,
        updatedName,
        updatedType,
        status,
        externalOrganizationProvider,
        externalOrganizationSubject
    );
  }

  /** Archives the workspace so it can no longer be selected as an active product scope. */
  public @NonNull Workspace archive() {
    requireActive();
    return new Workspace(
        workspaceId,
        name,
        type,
        WorkspaceStatus.ARCHIVED,
        externalOrganizationProvider,
        externalOrganizationSubject
    );
  }

  public boolean isActive() {
    return status == WorkspaceStatus.ACTIVE;
  }

  private void requireActive() {
    if (!isActive()) {
      throw new WorkspaceLifecycleException("archived workspace cannot be changed");
    }
  }
}
