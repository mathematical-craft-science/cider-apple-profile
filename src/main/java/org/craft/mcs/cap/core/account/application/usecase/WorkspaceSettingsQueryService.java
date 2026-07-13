package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.application.WorkspaceSettingsNotFoundException;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads effective settings from persisted workspace state rather than provisioning defaults. */
@Component
@RequiredArgsConstructor
public final class WorkspaceSettingsQueryService implements WorkspaceSettingsReader {

  private final @NonNull CurrentWorkspaceContextResolver currentWorkspaceContextResolver;
  private final @NonNull WorkspaceRepository workspaceRepository;
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
  public @NonNull WorkspaceSettingsView readByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    final var workspace = workspaceRepository.findByCiderMakerId(ciderMakerId)
        .orElseThrow(() -> new WorkspaceNotFoundException(ciderMakerId));
    return readByWorkspaceId(workspace.workspaceId());
  }

  private @NonNull WorkspaceSettingsView readByWorkspaceId(@NonNull WorkspaceId workspaceId) {
    final var settings = workspaceSettingsRepository.findByWorkspaceId(workspaceId)
        .orElseThrow(() -> new WorkspaceSettingsNotFoundException(workspaceId));
    return AccountViewMapper.toView(settings);
  }
}
