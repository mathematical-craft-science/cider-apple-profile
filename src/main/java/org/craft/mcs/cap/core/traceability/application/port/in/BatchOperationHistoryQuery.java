package org.craft.mcs.cap.core.traceability.application.port.in;

import java.util.UUID;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Identifies one material subject whose post-creation operation history is queried. */
public record BatchOperationHistoryQuery(
    @NonNull BatchTraceSubjectType subjectType,
    @NonNull UUID subjectId
) {

  public BatchOperationHistoryQuery {
    requireNonNull(subjectType, "batch trace subject type cannot be null");
    requireNonNull(subjectId, "batch trace subject id cannot be null");
  }
}
