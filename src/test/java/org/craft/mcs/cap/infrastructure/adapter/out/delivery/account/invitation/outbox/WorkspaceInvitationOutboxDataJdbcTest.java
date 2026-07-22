package org.craft.mcs.cap.infrastructure.adapter.out.delivery.account.invitation.outbox;

import io.namastack.outbox.OutboxJacksonAutoConfiguration;
import io.namastack.outbox.config.JdbcOutboxAutoConfiguration;
import io.namastack.outbox.config.OutboxCoreInfrastructureAutoConfiguration;
import io.namastack.outbox.config.OutboxCoreMulticasterAutoConfiguration;
import io.namastack.outbox.config.OutboxCoreProcessingAutoConfiguration;
import io.namastack.outbox.config.OutboxCoreSchedulingAutoConfiguration;
import io.namastack.outbox.config.OutboxCoreThreadingAutoConfiguration;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDeliveryRequest;
import org.craft.mcs.cap.core.account.invitation.application.port.in.DeliverWorkspaceInvitationEmailUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationDeliveryScheduler;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.infrastructure.adapter.in.outbox.account.invitation.WorkspaceInvitationEmailOutboxHandler;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.platform.persistence.SpringTransactionHelper;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@DataJdbcTest(properties = {
    "namastack.outbox.jdbc.schema-initialization.enabled=false",
    "namastack.outbox.multicaster.enabled=false",
    "namastack.outbox.polling.fixed.interval=20ms",
    "namastack.outbox.retry.policy=fixed",
    "namastack.outbox.retry.max-retries=2",
    "namastack.outbox.retry.fixed.delay=10ms",
    "namastack.outbox.retry.jitter=0ms"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration({
    OutboxCoreInfrastructureAutoConfiguration.class,
    OutboxCoreThreadingAutoConfiguration.class,
    OutboxCoreProcessingAutoConfiguration.class,
    OutboxCoreSchedulingAutoConfiguration.class,
    OutboxCoreMulticasterAutoConfiguration.class,
    OutboxJacksonAutoConfiguration.class,
    JdbcOutboxAutoConfiguration.class,
    TaskExecutionAutoConfiguration.class,
    TaskSchedulingAutoConfiguration.class
})
@Import({
    PostgresDataJdbcTestConfiguration.class,
    WorkspaceInvitationOutboxDataJdbcTest.TestSliceConfiguration.class,
    SpringTransactionHelper.class,
    NamastackWorkspaceInvitationDeliveryScheduler.class,
    WorkspaceInvitationEmailOutboxHandler.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class WorkspaceInvitationOutboxDataJdbcTest {

  private static final Instant NOW = Instant.parse("2026-07-21T12:00:00Z");
  private static final OffsetDateTime DATABASE_NOW = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);

  @Autowired
  private TransactionHelper transactionHelper;

  @Autowired
  private WorkspaceInvitationDeliveryScheduler deliveryScheduler;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private DeliverWorkspaceInvitationEmailUseCase deliverWorkspaceInvitationEmailUseCase;

  @Autowired
  private AdjustableClock outboxClock;

  @Test
  void persistsInvitationAndOutboxRecordInOneTransaction() {
    final var invitation = invitation();

    transactionHelper.executeInTransaction(() -> {
      insertInvitationDependencies(invitation);
      insertInvitation(invitation);
      deliveryScheduler.schedule(WorkspaceInvitationDeliveryRequest.from(invitation));
    });

    assertThat(counts(invitation))
        .returns(1L, PersistenceCounts::invitationCount)
        .returns(1L, PersistenceCounts::outboxCount);
  }

  @Test
  void rollsBackInvitationAndOutboxRecordTogether() {
    final var invitation = invitation();

    assertThatThrownBy(() -> transactionHelper.executeInTransaction(() -> {
      insertInvitationDependencies(invitation);
      insertInvitation(invitation);
      deliveryScheduler.schedule(WorkspaceInvitationDeliveryRequest.from(invitation));
      throw new ExpectedRollbackException();
    })).isInstanceOf(ExpectedRollbackException.class);

    assertThat(counts(invitation))
        .returns(0L, PersistenceCounts::invitationCount)
        .returns(0L, PersistenceCounts::outboxCount);
  }

  @Test
  void rejectsSchedulingOutsideInvitationTransaction() {
    final var request = WorkspaceInvitationDeliveryRequest.from(invitation());

    assertThatThrownBy(() -> deliveryScheduler.schedule(request))
        .isInstanceOf(IllegalTransactionStateException.class);
  }

  @Test
  void schedulesIndependentInvitationsForSameEmailInDifferentWorkspaces() {
    final var first = invitation();
    final var second = invitation();

    transactionHelper.executeInTransaction(() -> {
      persistAndSchedule(first);
      persistAndSchedule(second);
    });

    assertThat(first.invitedEmail()).isEqualTo(second.invitedEmail());
    assertThat(List.of(counts(first), counts(second)))
        .allSatisfy(counts -> assertThat(counts)
            .returns(1L, PersistenceCounts::invitationCount)
            .returns(1L, PersistenceCounts::outboxCount));
  }

  @Test
  void retriesSerializedDeliveryAfterTemporaryFailure() {
    final var invitation = invitation();
    final var request = WorkspaceInvitationDeliveryRequest.from(invitation);
    doThrow(new MailSendException("temporary SMTP failure"))
        .doNothing()
        .when(deliverWorkspaceInvitationEmailUseCase)
        .deliver(request);

    transactionHelper.executeInTransaction(() -> {
      insertInvitationDependencies(invitation);
      insertInvitation(invitation);
      deliveryScheduler.schedule(request);
    });

    verify(deliverWorkspaceInvitationEmailUseCase, timeout(10_000).times(1)).deliver(request);
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
        assertThat(outboxState(invitation).failureCount()).isEqualTo(1)
    );
    outboxClock.advance(Duration.ofSeconds(1));
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
        assertThat(outboxState(invitation))
            .returns("COMPLETED", OutboxState::status)
            .returns(1, OutboxState::failureCount)
    );
    verify(deliverWorkspaceInvitationEmailUseCase, timeout(10_000).times(2)).deliver(request);
  }

  private void persistAndSchedule(@NonNull WorkspaceInvitation invitation) {
    insertInvitationDependencies(invitation);
    insertInvitation(invitation);
    deliveryScheduler.schedule(WorkspaceInvitationDeliveryRequest.from(invitation));
  }

  private void insertInvitationDependencies(@NonNull WorkspaceInvitation invitation) {
    jdbcTemplate.update(
        """
            INSERT INTO account_users (
                user_id, identity_provider, external_subject, email, display_name, status,
                version, created_at, updated_at
            ) VALUES (?, 'CLERK', ?, 'owner@example.com', 'Owner', 'ACTIVE', 0, ?, ?)
            """,
        invitation.invitedByUserId().value(),
        "user_" + invitation.invitedByUserId().value(),
        DATABASE_NOW,
        DATABASE_NOW
    );
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (
                workspace_id, name, type, status, version, created_at, updated_at
            ) VALUES (?, 'Summer Cidery', 'CIDERY', 'ACTIVE', 0, ?, ?)
            """,
        invitation.workspaceId().value(),
        DATABASE_NOW,
        DATABASE_NOW
    );
  }

  private void insertInvitation(@NonNull WorkspaceInvitation invitation) {
    jdbcTemplate.update(
        """
            INSERT INTO workspace_invitations (
                invitation_id, workspace_id, invited_email, role, status, invited_by_user_id,
                expires_at, resend_count, version, created_at, updated_at
            ) VALUES (?, ?, ?, ?, 'PENDING', ?, ?, ?, 0, ?, ?)
            """,
        invitation.invitationId().value(),
        invitation.workspaceId().value(),
        invitation.invitedEmail().value(),
        invitation.role().name(),
        invitation.invitedByUserId().value(),
        OffsetDateTime.ofInstant(invitation.expiresAt(), ZoneOffset.UTC),
        invitation.resendCount(),
        DATABASE_NOW,
        DATABASE_NOW
    );
  }

  private @NonNull PersistenceCounts counts(@NonNull WorkspaceInvitation invitation) {
    return new PersistenceCounts(
        count(
            "SELECT COUNT(*) FROM workspace_invitations WHERE invitation_id = ?",
            invitation.invitationId().value()
        ),
        count(
            "SELECT COUNT(*) FROM outbox_record WHERE record_key = ?",
            invitation.invitationId().value().toString()
        )
    );
  }

  private @NonNull OutboxState outboxState(@NonNull WorkspaceInvitation invitation) {
    return jdbcTemplate.queryForObject(
        "SELECT status, failure_count, next_retry_at FROM outbox_record WHERE record_key = ?",
        (resultSet, _) -> new OutboxState(
            resultSet.getString("status"),
            resultSet.getInt("failure_count"),
            resultSet.getObject("next_retry_at", OffsetDateTime.class)
        ),
        invitation.invitationId().value().toString()
    );
  }

  private long count(
      @NonNull String sql,
      @NonNull Object parameter
  ) {
    final var count = jdbcTemplate.queryForObject(sql, Long.class, parameter);
    return count == null ? 0L : count; // NOSONAR
  }

  private static @NonNull WorkspaceInvitation invitation() {
    return WorkspaceInvitation.create(
        WorkspaceId.newId(),
        EmailAddress.of("invitee@example.com"),
        AccountRole.TECHNOLOGIST,
        AccountUserId.newId(),
        NOW,
        Duration.ofDays(7)
    );
  }

  private record PersistenceCounts(long invitationCount, long outboxCount) {
  }

  private record OutboxState(
      @NonNull String status,
      int failureCount,
      @NonNull OffsetDateTime nextRetryAt
  ) {
  }

  private static final class AdjustableClock extends Clock {

    private final @NonNull AtomicReference<@NonNull Instant> currentInstant;
    private final @NonNull ZoneId zone;

    private AdjustableClock(
        @NonNull AtomicReference<@NonNull Instant> currentInstant,
        @NonNull ZoneId zone
    ) {
      this.currentInstant = requireNonNull(currentInstant, "current instant cannot be null");
      this.zone = requireNonNull(zone, "clock zone cannot be null");
    }

    private static @NonNull AdjustableClock startingAt(@NonNull Instant instant) {
      return new AdjustableClock(
          new AtomicReference<>(requireNonNull(instant, "initial instant cannot be null")),
          ZoneOffset.UTC
      );
    }

    private void advance(@NonNull Duration duration) {
      currentInstant.updateAndGet(instant -> instant.plus(
          requireNonNull(duration, "clock advance duration cannot be null")
      ));
    }

    @Override
    public @NonNull ZoneId getZone() {
      return zone;
    }

    @Override
    public @NonNull Clock withZone(@NonNull ZoneId updatedZone) {
      return new AdjustableClock(
          currentInstant,
          requireNonNull(updatedZone, "updated clock zone cannot be null")
      );
    }

    @Override
    public @NonNull Instant instant() {
      return currentInstant.get();
    }
  }

  private static final class ExpectedRollbackException extends RuntimeException {
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class TestSliceConfiguration {

    @Bean
    @Primary
    @NonNull AdjustableClock outboxClock() {
      return AdjustableClock.startingAt(Instant.parse("2026-07-20T00:00:00Z"));
    }

    @Bean
    @NonNull DeliverWorkspaceInvitationEmailUseCase deliverWorkspaceInvitationEmailUseCase() {
      return mock(DeliverWorkspaceInvitationEmailUseCase.class);
    }
  }
}
