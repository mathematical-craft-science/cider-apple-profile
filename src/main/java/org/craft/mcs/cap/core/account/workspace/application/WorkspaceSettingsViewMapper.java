package org.craft.mcs.cap.core.account.workspace.application;

import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsState;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps persisted workspace settings state to the application read model. */
public final class WorkspaceSettingsViewMapper {

  private WorkspaceSettingsViewMapper() {
  }

  public static @NonNull WorkspaceSettingsView toView(@NonNull WorkspaceSettingsState state) {
    requireNonNull(state, "workspace settings state cannot be null");
    final var settings = state.settings();
    return new WorkspaceSettingsView(
        settings.workspaceId(),
        settings.measurementStaleAfter(),
        settings.defaultAcidityBasis(),
        state.revision(),
        state.createdAt(),
        state.updatedAt()
    );
  }
}
