package org.craft.mcs.cap.core.account.application.port.out;

import java.time.Instant;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.application.model.WorkspaceSettingsRevision;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Persisted workspace settings together with concurrency and audit metadata. */
public record WorkspaceSettingsState(
    @NonNull WorkspaceSettings settings,
    @NonNull WorkspaceSettingsRevision revision,
    @NonNull Instant createdAt,
    @NonNull Instant updatedAt
) {

  public WorkspaceSettingsState {
    requireNonNull(settings, "workspace settings cannot be null");
    requireNonNull(revision, "workspace settings revision cannot be null");
    requireNonNull(createdAt, "workspace settings creation time cannot be null");
    requireNonNull(updatedAt, "workspace settings update time cannot be null");
  }
}
