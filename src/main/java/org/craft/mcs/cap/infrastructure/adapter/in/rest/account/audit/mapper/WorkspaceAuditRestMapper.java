package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit.mapper;

import java.util.List;
import org.craft.mcs.cap.core.account.audit.application.port.in.WorkspaceAdministrativeAuditView;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit.dto.WorkspaceAdministrativeAuditListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit.dto.WorkspaceAdministrativeAuditResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Maps immutable workspace administration audit facts. */
public final class WorkspaceAuditRestMapper {

  private WorkspaceAuditRestMapper() {
  }

  public static @NonNull WorkspaceAdministrativeAuditResponse toResponse(
      @NonNull WorkspaceAdministrativeAuditView view
  ) {
    return new WorkspaceAdministrativeAuditResponse(
        view.auditEntryId().value(),
        view.actorUserId().value(),
        view.action(),
        view.subjectType(),
        view.subjectId().value(),
        auditValue(view.previousValue()),
        auditValue(view.newValue()),
        view.occurredAt()
    );
  }

  public static @NonNull WorkspaceAdministrativeAuditListResponse toListResponse(
      @NonNull List<@NonNull WorkspaceAdministrativeAuditView> views
  ) {
    return new WorkspaceAdministrativeAuditListResponse(
        views.stream().map(WorkspaceAuditRestMapper::toResponse).toList()
    );
  }

  private static @Nullable String auditValue(@Nullable WorkspaceAdministrativeAuditValue value) {
    return value == null ? null : value.value();
  }
}
