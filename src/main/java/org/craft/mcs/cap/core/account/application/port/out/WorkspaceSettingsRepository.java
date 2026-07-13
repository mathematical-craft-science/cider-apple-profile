package org.craft.mcs.cap.core.account.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Aggregate repository port for workspace-level product settings. */
public interface WorkspaceSettingsRepository {

  @NonNull WorkspaceSettings save(@NonNull WorkspaceSettings settings);

  @NonNull Optional<@NonNull WorkspaceSettings> findByWorkspaceId(@NonNull WorkspaceId workspaceId);
}
