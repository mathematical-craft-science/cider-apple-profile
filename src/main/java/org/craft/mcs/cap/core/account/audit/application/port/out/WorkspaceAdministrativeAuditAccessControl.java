package org.craft.mcs.cap.core.account.audit.application.port.out;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/**
 * Authorizes access to workspace administrative audit history without exposing the membership model to the audit
 * slice.
 */
public interface WorkspaceAdministrativeAuditAccessControl {

  void requireReadAccess(@NonNull WorkspaceId workspaceId);
}
