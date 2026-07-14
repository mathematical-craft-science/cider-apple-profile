package org.craft.mcs.cap.account.support;

import java.util.function.Supplier;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;


/** Executes application actions directly in focused unit tests. */
public final class DirectTransactionHelper implements TransactionHelper {

  public static final @NonNull TransactionHelper INSTANCE = new DirectTransactionHelper();

  private DirectTransactionHelper() {
  }

  @Override
  public void executeInTransaction(@NonNull Runnable action) {
    action.run();
  }

  @Override
  public <T> @NonNull T callInTransactionNonNull(@NonNull Supplier<T> action) {
    return action.get();
  }
}
