package org.craft.mcs.cap.core.account.audit.domain.model.value;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;

/** Stable identifier of one append-only workspace administration audit entry. */
@ValueObject
public record WorkspaceAuditEntryId(@NonNull UUID value) {

  public WorkspaceAuditEntryId {
    requireNonNull(value, "workspace audit entry id cannot be null");
  }

  public static @NonNull WorkspaceAuditEntryId newId() {
    return new WorkspaceAuditEntryId(newUuid());
  }

  public static @NonNull WorkspaceAuditEntryId of(@NonNull UUID value) {
    return new WorkspaceAuditEntryId(value);
  }
}
