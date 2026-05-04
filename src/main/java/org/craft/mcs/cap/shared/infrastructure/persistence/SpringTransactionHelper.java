package org.craft.mcs.cap.shared.infrastructure.persistence;

import java.util.function.Supplier;
import org.craft.mcs.cap.shared.application.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/**
 * Spring-реализация транзакционного помощника.
 */
@Component
public class SpringTransactionHelper implements TransactionHelper {

  @Transactional
  @Override
  public @NonNull <T> T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
    return requireNonNull(requireNonNull(supplier).get());
  }

  @Transactional
  @Override
  public void executeInTransaction(@NonNull Runnable runnable) {
    requireNonNull(runnable).run();
  }
}
