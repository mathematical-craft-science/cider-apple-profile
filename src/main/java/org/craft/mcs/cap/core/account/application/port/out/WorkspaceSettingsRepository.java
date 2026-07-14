package org.craft.mcs.cap.core.account.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.application.model.WorkspaceSettingsRevision;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Aggregate repository port for workspace-level product settings. */
public interface WorkspaceSettingsRepository {

  @NonNull WorkspaceSettingsState create(@NonNull WorkspaceSettings settings);

  @NonNull Optional<@NonNull WorkspaceSettingsState> update(
      @NonNull WorkspaceSettings settings,
      @NonNull WorkspaceSettingsRevision expectedRevision
  );

  @NonNull Optional<@NonNull WorkspaceSettingsState> findByWorkspaceId(
      @NonNull WorkspaceId workspaceId
  );
}
