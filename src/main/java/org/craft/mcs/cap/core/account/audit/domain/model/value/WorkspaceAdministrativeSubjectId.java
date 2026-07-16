package org.craft.mcs.cap.core.account.audit.domain.model.value;

import java.util.UUID;
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
}
