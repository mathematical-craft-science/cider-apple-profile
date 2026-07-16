package org.craft.mcs.cap.core.account.workspace.application.port.out;

import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.jspecify.annotations.NonNull;


/** Workspace-owned outbound boundary for recording administrative history. */
public interface WorkspaceAuditTrail {

  void recordCreated(
      @NonNull AccountUserId actorUserId,
      @NonNull Workspace workspace
  );

  void recordUpdated(
      @NonNull AccountUserId actorUserId,
      @NonNull Workspace previous,
      @NonNull Workspace updated
  );

  void recordArchived(
      @NonNull AccountUserId actorUserId,
      @NonNull Workspace previous,
      @NonNull Workspace archived
  );

  void recordSettingsUpdated(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceSettings previous,
      @NonNull WorkspaceSettings updated
  );
}
