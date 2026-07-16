package org.craft.mcs.cap.core.account.invitation.domain.model.value;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;

/** Stable identifier of one workspace invitation lifecycle. */
@ValueObject
public record WorkspaceInvitationId(@NonNull UUID value) {

  public WorkspaceInvitationId {
    requireNonNull(value, "workspace invitation id cannot be null");
  }

  public static @NonNull WorkspaceInvitationId newId() {
    return new WorkspaceInvitationId(newUuid());
  }

  public static @NonNull WorkspaceInvitationId of(@NonNull UUID value) {
    return new WorkspaceInvitationId(value);
  }
}
