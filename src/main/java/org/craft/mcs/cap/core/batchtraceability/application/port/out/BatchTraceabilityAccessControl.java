package org.craft.mcs.cap.core.batchtraceability.application.port.out;

import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Authorizes access to traceability facts and material-state transitions owned by a workspace. */
public interface BatchTraceabilityAccessControl {

  void requireReadAccess(@NonNull WorkspaceId workspaceId);

  /**
   * Requires permissions for every material subject type affected by a traceability transition.
   */
  void requireWriteAccess(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull BatchTraceSubjectType> subjectTypes
  );
}
