package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.provisioning;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.craft.mcs.cap.core.account.provisioning.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
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
@Import({PostgresDataJdbcTestConfiguration.class, PostgresAccountProvisioningLock.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PostgresAccountProvisioningLockDataJdbcTest {

  @Autowired
  private AccountProvisioningLock lock;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Test
  void serializesOneExternalIdentityWithoutBlockingAnotherIdentity() throws Exception {
    final var sharedSubject = ExternalUserSubject.of("shared-subject");
    final var independentSubject = ExternalUserSubject.of("independent-subject");
    final var firstAcquired = new CountDownLatch(1);
    final var releaseFirst = new CountDownLatch(1);
    final var competingAttempted = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var first = executor.submit(() -> transaction().executeWithoutResult(_ -> {
        lock.acquire(IdentityProviderCode.KEYCLOAK, sharedSubject);
        firstAcquired.countDown();
        await(releaseFirst);
      }));
      assertThat(firstAcquired.await(10, TimeUnit.SECONDS)).isTrue();

      final var competing = executor.submit(() -> transaction().executeWithoutResult(_ -> {
        competingAttempted.countDown();
        lock.acquire(IdentityProviderCode.KEYCLOAK, sharedSubject);
      }));
      assertThat(competingAttempted.await(10, TimeUnit.SECONDS)).isTrue();
      assertThat(competing).isNotDone();

      final var independent = executor.submit(() -> transaction().executeWithoutResult(
          _ -> lock.acquire(IdentityProviderCode.KEYCLOAK, independentSubject)
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
        throw new AssertionError("timed out while waiting for provisioning lock test coordination");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("provisioning lock test was interrupted", exception);
    }
  }
}
