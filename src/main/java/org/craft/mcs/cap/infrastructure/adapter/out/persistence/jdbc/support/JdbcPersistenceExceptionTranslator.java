package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support;

import java.util.function.Supplier;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.OptimisticLockingFailureException;

import static java.util.Objects.requireNonNull;


/**
 * Anti-corruption helper that translates Spring Data JDBC persistence failures to CAP application
 * exceptions before they leave the outbound adapter.
 */
public final class JdbcPersistenceExceptionTranslator {

  private JdbcPersistenceExceptionTranslator() {
  }

  public static <T extends @NonNull Object> T translateOptimisticLocking(
      @NonNull String resourceName,
      @NonNull Supplier<T> operation
  ) {
    requireNonNull(resourceName, "resource name cannot be null");
    requireNonNull(operation, "persistence operation cannot be null");

    try {
      return operation.get();
    } catch (OptimisticLockingFailureException exception) {
      throw new ConcurrentResourceModificationException(resourceName, exception);
    }
  }

  public static void translateOptimisticLocking(
      @NonNull String resourceName,
      @NonNull Runnable operation
  ) {
    translateOptimisticLocking(resourceName, () -> {
      operation.run();
      return Boolean.TRUE;
    });
  }
}
