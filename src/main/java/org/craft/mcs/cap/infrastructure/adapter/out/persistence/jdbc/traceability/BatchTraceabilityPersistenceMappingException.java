package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability;

import java.io.Serial;
import org.jspecify.annotations.NonNull;


/** Indicates that persisted batch traceability data cannot be mapped to its JDBC representation. */
public final class BatchTraceabilityPersistenceMappingException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public BatchTraceabilityPersistenceMappingException(@NonNull String message) {
    super(message);
  }
}
