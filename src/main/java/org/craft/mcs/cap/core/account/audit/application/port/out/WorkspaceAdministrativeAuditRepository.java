package org.craft.mcs.cap.core.account.audit.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditEntry;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Append-only repository port for workspace administration audit facts. */
public interface WorkspaceAdministrativeAuditRepository {

  void append(@NonNull WorkspaceAdministrativeAuditEntry entry);

  @NonNull List<@NonNull WorkspaceAdministrativeAuditEntry> findAllByWorkspaceId(
      @NonNull WorkspaceId workspaceId
  );
}
