package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support;

import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Verifies that a JDBC aggregate row still has the revision observed by the write use case. */
public final class JdbcAggregateRevisionGuard {

  private JdbcAggregateRevisionGuard() {
  }

  public static void requireExpectedRevision(
      @NonNull String resourceName,
      @NonNull AggregateRevision expectedRevision,
      long actualRevision
  ) {
    requireNonNull(resourceName, "resource name cannot be null");
    requireNonNull(expectedRevision, "expected aggregate revision cannot be null");
    if (expectedRevision.value() != actualRevision) {
      throw new ConcurrentResourceModificationException(resourceName, null);
    }
  }
}
