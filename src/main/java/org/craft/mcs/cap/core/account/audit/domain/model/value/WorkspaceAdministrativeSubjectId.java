package org.craft.mcs.cap.core.account.audit.domain.model.value;

import java.util.UUID;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Identifies the workspace-owned subject referenced by an administrative audit fact. */
@ValueObject
public record WorkspaceAdministrativeSubjectId(@NonNull UUID value) {

  public WorkspaceAdministrativeSubjectId {
    requireNonNull(value, "workspace administrative subject id cannot be null");
  }

  /** Restores an audit subject identifier from persistence or transport data. */
  public static @NonNull WorkspaceAdministrativeSubjectId of(@NonNull UUID value) {
    return new WorkspaceAdministrativeSubjectId(value);
  }

  /** Creates a subject identifier referencing a workspace or its settings. */
  public static @NonNull WorkspaceAdministrativeSubjectId forWorkspace(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return of(workspaceId.value());
  }

  /** Creates a subject identifier referencing a workspace invitation. */
  public static @NonNull WorkspaceAdministrativeSubjectId forInvitation(
      @NonNull WorkspaceInvitationId invitationId
  ) {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    return of(invitationId.value());
  }

  /** Creates a subject identifier referencing a workspace membership. */
  public static @NonNull WorkspaceAdministrativeSubjectId forMembership(
      @NonNull WorkspaceMembershipId membershipId
  ) {
    requireNonNull(membershipId, "workspace membership id cannot be null");
    return of(membershipId.value());
  }
}
