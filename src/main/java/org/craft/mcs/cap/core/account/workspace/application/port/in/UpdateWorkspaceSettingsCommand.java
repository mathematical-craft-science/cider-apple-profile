package org.craft.mcs.cap.core.account.workspace.application.port.in;

import java.time.Duration;
import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsRevision;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Partial workspace-settings update guarded by the revision observed by the client. */
public record UpdateWorkspaceSettingsCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull WorkspaceSettingsRevision expectedRevision,
    @Nullable Duration measurementStaleAfter,
    @Nullable TitratableAcidityBasis defaultAcidityBasis
) {

  public UpdateWorkspaceSettingsCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(expectedRevision, "expected workspace settings revision cannot be null");
    if (measurementStaleAfter == null && defaultAcidityBasis == null) {
      throw new ApplicationValidationException("workspace settings update must contain at least one value");
    }
  }
}
