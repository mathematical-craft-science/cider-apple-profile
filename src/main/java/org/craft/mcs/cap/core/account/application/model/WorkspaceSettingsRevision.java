package org.craft.mcs.cap.core.account.application.model;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;


/** Optimistic-concurrency token exposed by the workspace-settings application boundary. */
public record WorkspaceSettingsRevision(long value) {

  public WorkspaceSettingsRevision {
    if (value < 0) {
      throw new ApplicationValidationException("workspace settings revision cannot be negative");
    }
  }

  public static @NonNull WorkspaceSettingsRevision of(long value) {
    return new WorkspaceSettingsRevision(value);
  }
}
