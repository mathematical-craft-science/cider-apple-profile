package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.provisioning.application.contract.WorkspaceProvisioningDefaults;
import org.craft.mcs.cap.core.account.provisioning.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.core.account.provisioning.application.usecase.EnsureCurrentAccountProvisionedService;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.contract.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsDefaults;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcAccountRepositoriesDataJdbcTest.AccountJdbcAdaptersTestConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class EnsureCurrentAccountProvisionedAtomicityDataJdbcTest {

  @Autowired
  private AccountProvisioningLock accountProvisioningLock;

  @Autowired
  private AccountUserRepository accountUserRepository;

  @Autowired
  private WorkspaceRepository workspaceRepository;

  @Autowired
  private WorkspaceMembershipRepository membershipRepository;

  @Autowired
  private WorkspaceInvitationRepository invitationRepository;

  @Autowired
  private WorkspaceSettingsRepository workspaceSettingsRepository;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void rollsBackEveryProvisionedAggregateWhenPreferencesCreationFails() {
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    when(preferencesRepository.create(any()))
        .thenThrow(new IllegalStateException("preferences persistence failed"));
    final var actor = new AuthenticatedActor(
        IdentityProviderCode.KEYCLOAK,
        ExternalUserSubject.of("rollback-provisioning-user"),
        EmailAddress.of("rollback.provisioning@example.com"),
        DisplayName.of("Rollback provisioning user"),
        null,
        null
    );
    final var actorProvider = mock(CurrentActorProvider.class);
    when(actorProvider.currentActor()).thenReturn(Optional.of(actor));
    final var service = new EnsureCurrentAccountProvisionedService(
        actorProvider,
        accountProvisioningLock,
        accountUserRepository,
        mock(CurrentAccountUserRequestCache.class),
        workspaceRepository,
        membershipRepository,
        invitationRepository,
        workspaceSettingsRepository,
        preferencesRepository,
        new WorkspaceProvisioningDefaults(
            DisplayName.of("Provisioned workspace"),
            WorkspaceType.CIDERY,
            new WorkspaceSettingsDefaults(
                Duration.ofDays(14),
                TitratableAcidityBasis.MALIC_ACID
            )
        ),
        transactionHelper(),
        Clock.fixed(Instant.parse("2026-08-10T08:00:00Z"), ZoneOffset.UTC)
    );

    assertThatThrownBy(service::ensureProvisioned)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("preferences persistence failed");
    assertThat(persistenceCounts())
        .returns(0L, PersistenceCounts::accountUsers)
        .returns(0L, PersistenceCounts::workspaces)
        .returns(0L, PersistenceCounts::workspaceSettings)
        .returns(0L, PersistenceCounts::workspaceMemberships);
  }

  private @NonNull PersistenceCounts persistenceCounts() {
    return requireNonNull(jdbcTemplate.queryForObject(
        """
            SELECT (SELECT COUNT(*) FROM account_users) AS account_users,
                   (SELECT COUNT(*) FROM workspaces) AS workspaces,
                   (SELECT COUNT(*) FROM workspace_settings) AS workspace_settings,
                   (SELECT COUNT(*) FROM workspace_memberships) AS workspace_memberships
            """,
        (resultSet, _) -> new PersistenceCounts(
            resultSet.getLong("account_users"),
            resultSet.getLong("workspaces"),
            resultSet.getLong("workspace_settings"),
            resultSet.getLong("workspace_memberships")
        )
    ));
  }

  private @NonNull TransactionHelper transactionHelper() {
    final var transactions = new TransactionTemplate(transactionManager);
    return new TransactionHelper() {
      @Override
      public <T> @NonNull T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
        return requireNonNull(transactions.execute(_ -> supplier.get()));
      }

      @Override
      public void executeInTransaction(@NonNull Runnable runnable) {
        transactions.executeWithoutResult(_ -> runnable.run());
      }
    };
  }

  private record PersistenceCounts(
      long accountUsers,
      long workspaces,
      long workspaceSettings,
      long workspaceMemberships
  ) {
  }
}
