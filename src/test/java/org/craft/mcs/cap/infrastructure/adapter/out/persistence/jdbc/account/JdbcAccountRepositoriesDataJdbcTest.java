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
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.PhoneNumber;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.workspace.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcAccountUserRepository.class,
    JdbcWorkspaceRepository.class,
    JdbcWorkspaceMembershipRepository.class,
    JdbcWorkspaceSettingsRepository.class,
    JdbcUserPreferencesRepository.class,
    JdbcContributorProfileRepository.class,
    JdbcWorkspaceInvitationRepository.class,
    JdbcWorkspaceAdministrativeAuditRepository.class,
    PostgresAccountProvisioningLock.class,
    PostgresWorkspaceAdministrationLock.class
})
class JdbcAccountRepositoriesDataJdbcTest {

  @Autowired
  private JdbcAccountUserRepository userRepository;

  @Autowired
  private JdbcWorkspaceRepository workspaceRepository;

  @Autowired
  private JdbcWorkspaceMembershipRepository membershipRepository;

  @Autowired
  private JdbcWorkspaceSettingsRepository settingsRepository;

  @Autowired
  private JdbcUserPreferencesRepository preferencesRepository;

  @Autowired
  private JdbcContributorProfileRepository contributorProfileRepository;

  @Autowired
  private JdbcWorkspaceInvitationRepository invitationRepository;

  @Autowired
  private JdbcWorkspaceAdministrativeAuditRepository auditRepository;

  @Autowired
  private PostgresAccountProvisioningLock accountProvisioningLock;

  @Test
  void persistsProviderNeutralAccountWorkspaceAndMembershipAggregates() {
    accountProvisioningLock.acquire(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123")
    );
    final var user = userRepository.save(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123"),
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("CAP Technologist"),
        null
    ));
    final var workspace = workspaceRepository.save(Workspace.createDefault(
        DisplayName.of("My cidery"),
        WorkspaceType.CIDERY,
        IdentityProviderCode.CLERK,
        ExternalOrganizationSubject.of("org_123")
    ));
    final var membership = membershipRepository.save(WorkspaceMembership.createOwner(
        workspace.workspaceId(),
        user.userId()
    ));
    final var settings = settingsRepository.create(new WorkspaceSettings(
        workspace.workspaceId(),
        Duration.ofHours(36),
        TitratableAcidityBasis.MALIC_ACID
    ));
    final var preferences = preferencesRepository.save(UserPreferences.defaults(user.userId()));
    final var contributorProfile = contributorProfileRepository.save(new ContributorProfile(
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
    final var user = userRepository.save(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("audit_user"),
        EmailAddress.of("owner@example.com"),
        DisplayName.of("Workspace Owner"),
        null
    ));
    final var workspace = workspaceRepository.save(Workspace.create(
        DisplayName.of("Audit workspace"),
        WorkspaceType.INSTITUTE
    ));
    membershipRepository.save(WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId()));
    final var invitation = invitationRepository.save(WorkspaceInvitation.create(
        workspace.workspaceId(),
        EmailAddress.of("invitee@example.com"),
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
        WorkspaceAdministrativeSubjectId.forInvitation(invitation.invitationId()),
        null,
        WorkspaceAdministrativeAuditValue.of(invitation.role().name()),
        Instant.parse("2026-07-14T08:00:00Z")
    );
    auditRepository.append(auditEntry);

    assertThat(invitationRepository.findById(invitation.invitationId())).contains(invitation);
    assertThat(auditRepository.findAllByWorkspaceId(workspace.workspaceId())).containsExactly(auditEntry);
  }

  @Test
  void updatesExistingAccountUserWithoutReinsertingAssignedId() {
    final var user = userRepository.save(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_update"),
        EmailAddress.of("before@example.com"),
        DisplayName.of("Before"),
        null
    ));
    final var updated = user.synchronizeProviderEmail(EmailAddress.of("after@example.com"));

    userRepository.save(updated);

    assertThat(userRepository.findById(user.userId())).contains(updated);
  }

  @Test
  void updatesExistingUserPreferencesWithoutReinsertingAssignedId() {
    final var user = userRepository.save(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("preferences_update"),
        EmailAddress.of("preferences@example.com"),
        DisplayName.of("Preferences"),
        null
    ));
    preferencesRepository.save(UserPreferences.defaults(user.userId()));
    final var updated = new UserPreferences(
        user.userId(),
        Locale.forLanguageTag("ru-RU"),
        ZoneId.of("Europe/Moscow")
    );

    preferencesRepository.save(updated);

    assertThat(preferencesRepository.findByUserId(user.userId())).contains(updated);
  }

  @Test
  void updatesWorkspaceSettingsWithAuditAndRejectsStaleRevision() {
    final var workspace = workspaceRepository.save(Workspace.createDefault(
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
}
