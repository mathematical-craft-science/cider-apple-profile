package org.craft.mcs.cap.shared.platform;

import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;


/**
 * Помощник выполнения транзакционных действий.
 */
public interface TransactionHelper {

  /**
   * Выполнить действие в транзакции и вернуть результат.
   *
   * @param supplier действие для выполнения.
   * @param <T>      тип результат выполнения.
   * @return результат выполнения.
   */
  @NonNull
  <T> T callInTransactionNonNull(@NonNull Supplier<T> supplier);

  /**
   * Выполнить действие в транзакции.
   *
   * @param runnable действие для выполнения.
   */
  void executeInTransaction(@NonNull Runnable runnable);

}
