package org.craft.mcs.cap.core.account.audit.application.port.in;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Reads the append-only administration history visible in one workspace. */
public interface ListWorkspaceAdministrativeAuditUseCase {

  @NonNull List<@NonNull WorkspaceAdministrativeAuditView> list(@NonNull WorkspaceId workspaceId);
}
