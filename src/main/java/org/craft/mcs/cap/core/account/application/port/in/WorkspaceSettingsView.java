package org.craft.mcs.cap.core.account.application.port.in;

import java.time.Duration;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Effective product settings persisted for one CAP workspace. */
public record WorkspaceSettingsView(
    @NonNull WorkspaceId workspaceId,
    @NonNull Duration measurementStaleAfter,
    @NonNull TitratableAcidityBasis defaultAcidityBasis
) {

  public WorkspaceSettingsView {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(measurementStaleAfter, "measurement stale duration cannot be null");
    requireNonNull(defaultAcidityBasis, "default acidity basis cannot be null");
  }
}
