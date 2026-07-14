package org.craft.mcs.cap.core.account.domain.model;

import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
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
    @Nullable IdentityProviderCode externalOrganizationProvider,
    @Nullable ExternalOrganizationSubject externalOrganizationSubject
) {

  public Workspace {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "workspace name cannot be null");
    requireNonNull(type, "workspace type cannot be null");
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
        externalOrganizationProvider,
        externalOrganizationSubject
    );
  }
}
