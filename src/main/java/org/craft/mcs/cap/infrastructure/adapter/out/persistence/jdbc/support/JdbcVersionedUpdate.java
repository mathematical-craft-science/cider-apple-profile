package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support;

import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Completes an atomic aggregate update after the database has checked its expected revision. */
public final class JdbcVersionedUpdate {

  private JdbcVersionedUpdate() {
  }

  public static <T extends @NonNull Object> @NonNull VersionedAggregate<T> complete(
      @NonNull String resourceName,
      @NonNull VersionedAggregate<T> attemptedUpdate,
      int affectedRows
  ) {
    requireNonNull(attemptedUpdate, "attempted aggregate update cannot be null");
    requireSingleRow(resourceName, affectedRows);
    return new VersionedAggregate<>(
        attemptedUpdate.aggregate(),
        AggregateRevision.of(attemptedUpdate.revision().value() + 1)
    );
  }

  public static void requireSingleRow(
      @NonNull String resourceName,
      int affectedRows
  ) {
    requireNonNull(resourceName, "resource name cannot be null");
    if (affectedRows != 1) {
      throw new ConcurrentResourceModificationException(resourceName, null);
    }
  }
}
