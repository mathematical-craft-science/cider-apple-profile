package org.craft.mcs.cap.core.traceability.application;

import java.io.Serial;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;


/** Raised when no immutable trace fact contains the requested subject. */
@SuppressWarnings("java:S110")
public final class BatchTraceSubjectNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public BatchTraceSubjectNotFoundException(@NonNull BatchTraceSubject subject) {
    super("batch trace subject not found: " + subject.type() + ':' + subject.id());
  }
}
