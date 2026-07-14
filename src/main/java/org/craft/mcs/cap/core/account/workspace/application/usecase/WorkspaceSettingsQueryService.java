package org.craft.mcs.cap.core.account.workspace.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.membership.application.usecase.CurrentWorkspaceContextResolver;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceSettingsNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/** Reads effective settings from persisted workspace state rather than provisioning defaults. */
@Component
@RequiredArgsConstructor
public final class WorkspaceSettingsQueryService implements WorkspaceSettingsReader {

  private final @NonNull CurrentWorkspaceContextResolver currentWorkspaceContextResolver;
  private final @NonNull WorkspaceSettingsRepository workspaceSettingsRepository;

  @Override
  public @NonNull WorkspaceSettingsView readCurrent() {
    final var workspaceId = currentWorkspaceContextResolver.resolve()
        .activeMembership()
        .workspace()
        .workspaceId();
    return readByWorkspaceId(workspaceId);
  }

  @Override
  public @NonNull WorkspaceSettingsView readByWorkspaceId(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    final var state = workspaceSettingsRepository.findByWorkspaceId(workspaceId)
        .orElseThrow(() -> new WorkspaceSettingsNotFoundException(workspaceId));
    return WorkspaceSettingsView.from(state);
  }
}
