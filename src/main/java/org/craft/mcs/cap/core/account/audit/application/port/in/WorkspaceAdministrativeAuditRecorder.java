package org.craft.mcs.cap.core.account.audit.application.port.in;

import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Records an immutable actor-aware fact about workspace administration. */
public interface WorkspaceAdministrativeAuditRecorder {

  void append(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceAdministrativeAction action,
      @NonNull WorkspaceAdministrativeSubjectType subjectType,
      @NonNull WorkspaceAdministrativeSubjectId subjectId,
      @Nullable WorkspaceAdministrativeAuditValue previousValue,
      @Nullable WorkspaceAdministrativeAuditValue newValue
  );
}
