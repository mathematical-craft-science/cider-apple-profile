package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditEntry;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.audit.application.port.out.WorkspaceAdministrativeAuditRepository;
import org.craft.mcs.cap.core.account.contributor.application.port.out.ContributorProfileRepository;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.PhoneNumber;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.shared.domain.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.core.account.provisioning.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcAccountRepositoriesDataJdbcTest.AccountJdbcAdaptersTestConfiguration.class
})
class JdbcAccountRepositoriesDataJdbcTest {

  @Autowired
  private AccountUserRepository userRepository;

  @Autowired
  private WorkspaceRepository workspaceRepository;

  @Autowired
  private WorkspaceMembershipRepository membershipRepository;

  @Autowired
  private WorkspaceSettingsRepository settingsRepository;

  @Autowired
  private UserPreferencesRepository preferencesRepository;

  @Autowired
  private ContributorProfileRepository contributorProfileRepository;

  @Autowired
  private WorkspaceInvitationRepository invitationRepository;

  @Autowired
  private WorkspaceAdministrativeAuditRepository auditRepository;

  @Autowired
  private AccountProvisioningLock accountProvisioningLock;

  @Test
  void persistsProviderNeutralAccountWorkspaceAndMembershipAggregates() {
    accountProvisioningLock.acquire(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123")
    );
    final var user = userRepository.create(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123"),
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("CAP Technologist"),
        null
    ));
    final var workspace = workspaceRepository.create(Workspace.createDefault(
        DisplayName.of("My cidery"),
        WorkspaceType.CIDERY,
        IdentityProviderCode.CLERK,
        ExternalOrganizationSubject.of("org_123")
    ));
    final var membership = membershipRepository.create(WorkspaceMembership.createOwner(
        workspace.workspaceId(),
        user.userId()
    ));
    final var settings = settingsRepository.create(new WorkspaceSettings(
        workspace.workspaceId(),
        Duration.ofHours(36),
        TitratableAcidityBasis.MALIC_ACID
    ));
    final var preferences = preferencesRepository.create(UserPreferences.defaults(user.userId()));
    final var contributorProfile = contributorProfileRepository.create(new ContributorProfile(
        user.userId(),
        ContributorType.PRODUCER,
        DisplayName.of("CAP Orchard"),
        HttpUrl.of("https://example.com"),
        EmailAddress.of("orchard@example.com"),
        PhoneNumber.of("+1 555 0100"),
        ContactVisibility.PUBLIC,
        Instant.parse("2026-01-10T12:00:00Z")
    ));

    assertThat(userRepository.findByExternalIdentity(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123")
    )).contains(user);
    assertThat(workspaceRepository.findById(workspace.workspaceId())).contains(workspace);
    assertThat(membershipRepository.findByWorkspaceIdAndUserId(workspace.workspaceId(), user.userId()))
        .contains(membership);
    assertThat(membershipRepository.findAllByUserId(user.userId())).containsExactly(membership);
    assertThat(settingsRepository.findByWorkspaceId(workspace.workspaceId())).contains(settings);
    assertThat(preferencesRepository.findByUserId(user.userId())).contains(preferences);
    assertThat(contributorProfileRepository.findByUserId(user.userId())).contains(contributorProfile);
  }

  @Test
  void persistsWorkspaceInvitationAndAdministrativeAudit() {
    final var user = userRepository.create(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("audit_user"),
        EmailAddress.of("owner@example.com"),
        DisplayName.of("Workspace Owner"),
        null
    ));
    final var workspace = workspaceRepository.create(Workspace.create(
        DisplayName.of("Audit workspace"),
        WorkspaceType.INSTITUTE
    ));
    membershipRepository.create(WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId()));
    final var invitedEmail = EmailAddress.of("invitee@example.com");
    final var invitation = invitationRepository.create(WorkspaceInvitation.create(
        workspace.workspaceId(),
        invitedEmail,
        AccountRole.TECHNOLOGIST,
        user.userId(),
        Instant.parse("2026-07-14T08:00:00Z"),
        Duration.ofDays(7)
    ));
    final var auditEntry = WorkspaceAdministrativeAuditEntry.create(
        workspace.workspaceId(),
        user.userId(),
        WorkspaceAdministrativeAction.INVITATION_CREATED,
        WorkspaceAdministrativeSubjectType.INVITATION,
        WorkspaceAdministrativeSubjectId.of(invitation.invitationId().value()),
        null,
        WorkspaceAdministrativeAuditValue.of(invitation.role().name()),
        Instant.parse("2026-07-14T08:00:00Z")
    );
    auditRepository.append(auditEntry);

    assertThat(invitationRepository.findById(invitation.invitationId())).contains(invitation);
    assertThat(invitationRepository.findAllByInvitedEmail(invitedEmail)).containsExactly(invitation);
    assertThat(auditRepository.findAllByWorkspaceId(workspace.workspaceId())).containsExactly(auditEntry);
  }

  @Test
  void updatesExistingAccountUserWithoutReinsertingAssignedId() {
    final var user = userRepository.create(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_update"),
        EmailAddress.of("before@example.com"),
        DisplayName.of("Before"),
        null
    ));
    final var state = userRepository.findVersionedById(user.userId()).orElseThrow();
    final var updated = user.synchronizeProviderEmail(EmailAddress.of("after@example.com"));

    userRepository.update(state.withAggregate(updated));

    assertThat(userRepository.findById(user.userId())).contains(updated);
  }

  @Test
  void rejectsAccountUserStateLoadedBeforeAnotherUpdate() {
    final var user = userRepository.create(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_stale_update"),
        EmailAddress.of("before@example.com"),
        DisplayName.of("Before"),
        null
    ));
    final var first = userRepository.findVersionedById(user.userId()).orElseThrow();
    final var stale = userRepository.findVersionedById(user.userId()).orElseThrow();

    userRepository.update(first.withAggregate(
        first.aggregate().synchronizeProviderEmail(EmailAddress.of("first@example.com"))
    ));

    assertThatThrownBy(() -> userRepository.update(stale.withAggregate(
        stale.aggregate().synchronizeProviderEmail(EmailAddress.of("stale@example.com"))
    )))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: account user");
  }

  @Test
  void updatesExistingUserPreferencesWithoutReinsertingAssignedId() {
    final var user = userRepository.create(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("preferences_update"),
        EmailAddress.of("preferences@example.com"),
        DisplayName.of("Preferences"),
        null
    ));
    preferencesRepository.create(UserPreferences.defaults(user.userId()));
    final var updated = new UserPreferences(
        user.userId(),
        Locale.forLanguageTag("ru-RU"),
        ZoneId.of("Europe/Moscow")
    );

    final var state = preferencesRepository.findVersionedById(user.userId()).orElseThrow();
    preferencesRepository.update(state.withAggregate(updated));

    assertThat(preferencesRepository.findByUserId(user.userId())).contains(updated);
  }

  @Test
  void updatesWorkspaceSettingsWithAuditAndRejectsStaleRevision() {
    final var workspace = workspaceRepository.create(Workspace.createDefault(
        DisplayName.of("Settings workspace"),
        WorkspaceType.CIDERY,
        null,
        null
    ));
    final var created = settingsRepository.create(new WorkspaceSettings(
        workspace.workspaceId(),
        Duration.ofDays(14),
        TitratableAcidityBasis.MALIC_ACID
    ));
    final var changedSettings = created.settings().update(
        Duration.ofDays(21),
        TitratableAcidityBasis.CITRIC_ACID
    );

    final var updated = settingsRepository.update(changedSettings, created.revision()).orElseThrow();

    assertThat(updated)
        .returns(Duration.ofDays(21), state -> state.settings().measurementStaleAfter())
        .returns(TitratableAcidityBasis.CITRIC_ACID, state -> state.settings().defaultAcidityBasis())
        .matches(state -> state.revision().value() == created.revision().value() + 1)
        .matches(state -> !state.updatedAt().isBefore(created.updatedAt()));

    final var staleUpdate = changedSettings.update(Duration.ofDays(28), TitratableAcidityBasis.MALIC_ACID);
    assertThatThrownBy(() -> settingsRepository.update(staleUpdate, created.revision()))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: workspace settings");
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan("org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account")
  static class AccountJdbcAdaptersTestConfiguration {
  }
}
