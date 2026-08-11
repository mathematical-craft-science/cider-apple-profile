package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.administration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresDataJdbcTestConfiguration.class, PostgresWorkspaceAdministrationLock.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PostgresWorkspaceAdministrationLockDataJdbcTest {

  @Autowired
  private WorkspaceAdministrationLock lock;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Test
  void serializesOneWorkspaceWithoutBlockingAnotherWorkspace() throws Exception {
    final var lockedWorkspaceId = WorkspaceId.newId();
    final var independentWorkspaceId = WorkspaceId.newId();
    final var firstAcquired = new CountDownLatch(1);
    final var releaseFirst = new CountDownLatch(1);
    final var competingAttempted = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var first = executor.submit(() -> transaction().executeWithoutResult(_ -> {
        lock.acquire(lockedWorkspaceId);
        firstAcquired.countDown();
        await(releaseFirst);
      }));
      assertThat(firstAcquired.await(10, TimeUnit.SECONDS)).isTrue();

      final var competing = executor.submit(() -> transaction().executeWithoutResult(_ -> {
        competingAttempted.countDown();
        lock.acquire(lockedWorkspaceId);
      }));
      assertThat(competingAttempted.await(10, TimeUnit.SECONDS)).isTrue();
      assertThat(competing).isNotDone();

      final var independent = executor.submit(() -> transaction().executeWithoutResult(
          _ -> lock.acquire(independentWorkspaceId)
      ));
      independent.get(10, TimeUnit.SECONDS);

      releaseFirst.countDown();
      first.get(10, TimeUnit.SECONDS);
      competing.get(10, TimeUnit.SECONDS);
    } finally {
      releaseFirst.countDown();
    }
  }

  private TransactionTemplate transaction() {
    return new TransactionTemplate(transactionManager);
  }

  private static void await(CountDownLatch latch) {
    try {
      if (!latch.await(10, TimeUnit.SECONDS)) {
        throw new AssertionError("timed out while waiting for workspace lock test coordination");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("workspace lock test was interrupted", exception);
    }
  }
}
