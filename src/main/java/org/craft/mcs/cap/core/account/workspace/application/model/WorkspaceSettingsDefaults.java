package org.craft.mcs.cap.core.account.workspace.application.model;

import java.time.Duration;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Initial product settings copied into a newly provisioned workspace. */
public record WorkspaceSettingsDefaults(
    @NonNull Duration measurementStaleAfter,
    @NonNull TitratableAcidityBasis defaultAcidityBasis
) {

  public WorkspaceSettingsDefaults {
    requireNonNull(measurementStaleAfter, "measurement stale duration cannot be null");
    requireNonNull(defaultAcidityBasis, "default acidity basis cannot be null");
  }
}
