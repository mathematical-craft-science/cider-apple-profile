package org.craft.mcs.cap.core.account.audit.application.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.in.WorkspaceAdministrativeAuditRecorder;
import org.craft.mcs.cap.core.account.audit.application.port.out.WorkspaceAdministrativeAuditRepository;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditEntry;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Appends actor-aware workspace administration facts using the application clock. */
@Component
@RequiredArgsConstructor
public class WorkspaceAdministrativeAuditAppender implements WorkspaceAdministrativeAuditRecorder {

  private final @NonNull WorkspaceAdministrativeAuditRepository auditRepository;
  private final @NonNull Clock clock;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void append(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceAdministrativeAction action,
      @NonNull WorkspaceAdministrativeSubjectType subjectType,
      @NonNull WorkspaceAdministrativeSubjectId subjectId,
      @Nullable WorkspaceAdministrativeAuditValue previousValue,
      @Nullable WorkspaceAdministrativeAuditValue newValue
  ) {
    auditRepository.append(WorkspaceAdministrativeAuditEntry.create(
        workspaceId,
        actorUserId,
        action,
        subjectType,
        subjectId,
        previousValue,
        newValue,
        clock.instant()
    ));
  }
}
