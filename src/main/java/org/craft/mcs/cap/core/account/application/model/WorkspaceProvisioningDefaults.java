package org.craft.mcs.cap.core.account.application.model;

import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Initial workspace identity and product settings used only during lazy account provisioning. */
public record WorkspaceProvisioningDefaults(
    @NonNull DisplayName name,
    @NonNull WorkspaceType type,
    @NonNull WorkspaceSettingsDefaults settings
) {

  public WorkspaceProvisioningDefaults {
    requireNonNull(name, "default workspace name cannot be null");
    requireNonNull(type, "default workspace type cannot be null");
    requireNonNull(settings, "default workspace settings cannot be null");
  }
}
