package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.actor.application.contract.ActorDisplayView;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditEntry;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.audit.application.port.out.WorkspaceAdministrativeAuditRepository;
import org.craft.mcs.cap.core.account.actor.application.contract.ActorLifecycleStatus;
import org.craft.mcs.cap.core.account.actor.application.port.out.ActorDirectoryEntryReader;
import org.craft.mcs.cap.core.account.context.application.contract.WorkspaceMembershipSnapshot;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentAccountWorkspaceQuery;
import org.craft.mcs.cap.core.account.contributor.application.port.out.ContributorProfileRepository;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
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
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;
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

  @Autowired
  private CurrentAccountWorkspaceQuery currentAccountWorkspaceQuery;

  @Autowired
  private ActorDirectoryEntryReader accountUserActorDirectoryEntryReader;

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
    assertThat(membershipRepository.findVersionedByWorkspaceIdAndUserId(workspace.workspaceId(), user.userId())
        .map(VersionedAggregate::aggregate))
        .contains(membership);
    assertThat(membershipRepository.findAllByWorkspaceId(workspace.workspaceId()))
        .containsExactly(membership);
    assertThat(currentAccountWorkspaceQuery.findActiveByUserId(user.userId()))
        .singleElement()
        .returns(membership.membershipId(), WorkspaceMembershipSnapshot::membershipId)
        .returns(workspace.workspaceId(), WorkspaceMembershipSnapshot::workspaceId)
        .returns(workspace.name(), WorkspaceMembershipSnapshot::workspaceName)
        .returns(membership.role(), WorkspaceMembershipSnapshot::role);
    assertThat(settingsRepository.findByWorkspaceId(workspace.workspaceId())).contains(settings);
    assertThat(preferencesRepository.findByUserId(user.userId())).contains(preferences);
    assertThat(contributorProfileRepository.findByUserId(user.userId())).contains(contributorProfile);
  }

  @Test
  void findsAllProviderIdentitiesSharingOneNormalizedEmail() {
    final var email = EmailAddress.of("Shared.Identity@Example.com");
    final var clerkUser = userRepository.create(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("shared-email-clerk"),
        email,
        DisplayName.of("Clerk identity"),
        null
    ));
    final var keycloakUser = userRepository.create(AccountUser.create(
        IdentityProviderCode.KEYCLOAK,
        ExternalUserSubject.of("shared-email-keycloak"),
        email,
        DisplayName.of("Keycloak identity"),
        null
    ));

    assertThat(userRepository.findAllByEmail(EmailAddress.of("SHARED.IDENTITY@example.com")))
        .containsExactlyInAnyOrder(clerkUser, keycloakUser);
  }

  @Test
  void resolvesActiveAndDeletedAccountUsersAsDurableActorEntries() {
    final var user = userRepository.create(AccountUser.create(
        IdentityProviderCode.KEYCLOAK,
        ExternalUserSubject.of("actor-directory-user"),
        EmailAddress.of("actor@example.com"),
        DisplayName.of("Audit actor"),
        HttpUrl.of("https://example.com/audit-actor.png")
    ));
    final var workspace = workspaceRepository.create(Workspace.createDefault(
        DisplayName.of("Actor directory workspace"),
        WorkspaceType.CIDERY,
        null,
        null
    ));
    final var membership = membershipRepository.create(WorkspaceMembership.createOwner(
        workspace.workspaceId(),
        user.userId()
    ));

    assertThat(accountUserActorDirectoryEntryReader.findById(
        workspace.workspaceId(),
        user.userId().value()
    ))
        .get()
        .returns(user.userId().value(), view -> view.actor().id())
        .returns(DisplayName.of("Audit actor"), ActorDisplayView::displayName)
        .returns(ActorLifecycleStatus.ACTIVE, ActorDisplayView::status);

    final var state = userRepository.findVersionedById(user.userId()).orElseThrow();
    userRepository.update(state.withAggregate(state.aggregate().retainDeletedTombstone()));

    assertThat(accountUserActorDirectoryEntryReader.findById(
        workspace.workspaceId(),
        user.userId().value()
    ))
        .get()
        .returns(user.userId().value(), view -> view.actor().id())
        .returns(DisplayName.of("Deleted user"), ActorDisplayView::displayName)
        .returns(null, ActorDisplayView::avatarUrl)
        .returns(ActorLifecycleStatus.DELETED, ActorDisplayView::status);
    final var membershipState = membershipRepository.findVersionedById(membership.membershipId()).orElseThrow();
    membershipRepository.update(membershipState.withAggregate(membership.remove()));
    assertThat(membershipRepository.findAllByWorkspaceId(workspace.workspaceId())).isEmpty();
    assertThat(accountUserActorDirectoryEntryReader.findById(
        workspace.workspaceId(),
        user.userId().value()
    ))
        .get()
        .returns(ActorLifecycleStatus.DELETED, ActorDisplayView::status);
    assertThat(accountUserActorDirectoryEntryReader.findById(
        WorkspaceId.newId(),
        user.userId().value()
    )).isEmpty();
  }

  @Test
  void excludesSuspendedMembershipsFromCurrentAccountWorkspaceQuery() {
    final var user = userRepository.create(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("suspended_membership_user"),
        EmailAddress.of("suspended@example.com"),
        DisplayName.of("Suspended member"),
        null
    ));
    final var workspace = workspaceRepository.create(Workspace.create(
        DisplayName.of("Restricted workspace"),
        WorkspaceType.CIDERY
    ));
    final var owner = WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId());
    membershipRepository.create(new WorkspaceMembership(
        owner.membershipId(),
        owner.workspaceId(),
        owner.userId(),
        owner.role(),
        WorkspaceMembershipStatus.SUSPENDED
    ));

    assertThat(currentAccountWorkspaceQuery.findActiveByUserId(user.userId())).isEmpty();
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
    assertThat(userRepository.findById(user.userId()))
        .get()
        .extracting(AccountUser::email)
        .isEqualTo(EmailAddress.of("first@example.com"));
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
  void rejectsWorkspaceStateLoadedBeforeAnotherUpdate() {
    final var workspace = workspaceRepository.create(Workspace.create(
        DisplayName.of("Initial workspace"),
        WorkspaceType.CIDERY
    ));
    final var first = workspaceRepository.findVersionedById(workspace.workspaceId()).orElseThrow();
    final var stale = workspaceRepository.findVersionedById(workspace.workspaceId()).orElseThrow();

    workspaceRepository.update(first.withAggregate(
        first.aggregate().update(DisplayName.of("Winning workspace"), WorkspaceType.LABORATORY)
    ));

    assertThatThrownBy(() -> workspaceRepository.update(stale.withAggregate(
        stale.aggregate().update(DisplayName.of("Stale workspace"), WorkspaceType.INSTITUTE)
    )))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: workspace");
    assertThat(workspaceRepository.findById(workspace.workspaceId()))
        .get()
        .returns(DisplayName.of("Winning workspace"), Workspace::name)
        .returns(WorkspaceType.LABORATORY, Workspace::type);
  }

  @Test
  void rejectsWorkspaceMembershipStateLoadedBeforeAnotherUpdate() {
    final var user = persistUser("membership-revision");
    final var workspace = persistWorkspace("Membership revision workspace");
    final var membership = membershipRepository.create(WorkspaceMembership.createOwner(
        workspace.workspaceId(),
        user.userId()
    ));
    final var first = membershipRepository.findVersionedById(membership.membershipId()).orElseThrow();
    final var stale = membershipRepository.findVersionedById(membership.membershipId()).orElseThrow();

    membershipRepository.update(first.withAggregate(first.aggregate().demoteOwnerToAdmin()));

    assertThatThrownBy(() -> membershipRepository.update(stale.withAggregate(stale.aggregate().suspend())))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: workspace membership");
    assertThat(membershipRepository.findVersionedById(membership.membershipId()))
        .get()
        .extracting(VersionedAggregate::aggregate)
        .returns(AccountRole.ADMIN, WorkspaceMembership::role)
        .returns(WorkspaceMembershipStatus.ACTIVE, WorkspaceMembership::status);
  }

  @Test
  void rejectsUserPreferencesStateLoadedBeforeAnotherUpdate() {
    final var user = persistUser("preferences-revision");
    preferencesRepository.create(UserPreferences.defaults(user.userId()));
    final var first = preferencesRepository.findVersionedById(user.userId()).orElseThrow();
    final var stale = preferencesRepository.findVersionedById(user.userId()).orElseThrow();
    final var winning = new UserPreferences(
        user.userId(),
        Locale.forLanguageTag("ru-RU"),
        ZoneId.of("Europe/Moscow")
    );
    final var staleUpdate = new UserPreferences(
        user.userId(),
        Locale.forLanguageTag("de-DE"),
        ZoneId.of("Europe/Berlin")
    );

    preferencesRepository.update(first.withAggregate(winning));

    assertThatThrownBy(() -> preferencesRepository.update(stale.withAggregate(staleUpdate)))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: user preferences");
    assertThat(preferencesRepository.findByUserId(user.userId())).contains(winning);
  }

  @Test
  void rejectsContributorProfileStateLoadedBeforeAnotherUpdate() {
    final var user = persistUser("contributor-revision");
    final var consentedAt = Instant.parse("2026-01-10T12:00:00Z");
    final var profile = contributorProfileRepository.create(ContributorProfile.create(
        user.userId(),
        ContributorType.PRODUCER,
        DisplayName.of("Initial contributor"),
        null,
        EmailAddress.of("contributor@example.com"),
        null,
        true,
        consentedAt
    ));
    final var first = contributorProfileRepository.findVersionedById(user.userId()).orElseThrow();
    final var stale = contributorProfileRepository.findVersionedById(user.userId()).orElseThrow();
    final var winning = profile.update(
        ContributorType.LABORATORY,
        DisplayName.of("Winning contributor"),
        null,
        EmailAddress.of("winner@example.com"),
        null,
        true,
        consentedAt
    );
    final var staleUpdate = profile.update(
        ContributorType.RESEARCHER,
        DisplayName.of("Stale contributor"),
        null,
        EmailAddress.of("stale-contributor@example.com"),
        null,
        true,
        consentedAt
    );

    contributorProfileRepository.update(first.withAggregate(winning));

    assertThatThrownBy(() -> contributorProfileRepository.update(stale.withAggregate(staleUpdate)))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: contributor profile");
    assertThat(contributorProfileRepository.findByUserId(user.userId()))
        .get()
        .returns(DisplayName.of("Winning contributor"), ContributorProfile::publicName)
        .returns(ContributorType.LABORATORY, ContributorProfile::contributorType);
  }

  @Test
  void rejectsWorkspaceInvitationStateLoadedBeforeAnotherUpdate() {
    final var user = persistUser("invitation-revision");
    final var workspace = persistWorkspace("Invitation revision workspace");
    final var now = Instant.parse("2026-07-14T08:00:00Z");
    final var invitation = invitationRepository.create(WorkspaceInvitation.create(
        workspace.workspaceId(),
        EmailAddress.of("revision-invitee@example.com"),
        AccountRole.TECHNOLOGIST,
        user.userId(),
        now,
        Duration.ofDays(7)
    ));
    final var first = invitationRepository.findVersionedById(invitation.invitationId()).orElseThrow();
    final var stale = invitationRepository.findVersionedById(invitation.invitationId()).orElseThrow();

    invitationRepository.update(first.withAggregate(first.aggregate().reject(now)));

    assertThatThrownBy(() -> invitationRepository.update(stale.withAggregate(stale.aggregate().revoke(now))))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: workspace invitation");
    assertThat(invitationRepository.findById(invitation.invitationId()))
        .get()
        .extracting(WorkspaceInvitation::status)
        .isEqualTo(WorkspaceInvitationStatus.REJECTED);
  }

  @Test
  void persistedExpirationReleasesPendingInvitationEmailUniqueness() {
    final var user = persistUser("expired-invitation");
    final var workspace = persistWorkspace("Expired invitation workspace");
    final var invitedEmail = EmailAddress.of("expired-invitee@example.com");
    final var createdAt = Instant.parse("2026-07-01T08:00:00Z");
    final var elapsed = invitationRepository.create(WorkspaceInvitation.create(
        workspace.workspaceId(),
        invitedEmail,
        AccountRole.TECHNOLOGIST,
        user.userId(),
        createdAt,
        Duration.ofDays(1)
    ));
    final var elapsedState = invitationRepository.findVersionedById(elapsed.invitationId()).orElseThrow();

    invitationRepository.update(elapsedState.withAggregate(
        elapsedState.aggregate().expire(createdAt.plus(Duration.ofDays(2)))
    ));
    final var replacement = invitationRepository.create(WorkspaceInvitation.create(
        workspace.workspaceId(),
        invitedEmail,
        AccountRole.VIEWER,
        user.userId(),
        createdAt.plus(Duration.ofDays(2)),
        Duration.ofDays(7)
    ));

    assertThat(invitationRepository.findById(elapsed.invitationId()))
        .get()
        .extracting(WorkspaceInvitation::status)
        .isEqualTo(WorkspaceInvitationStatus.EXPIRED);
    assertThat(invitationRepository.findById(replacement.invitationId()))
        .get()
        .extracting(WorkspaceInvitation::status)
        .isEqualTo(WorkspaceInvitationStatus.PENDING);
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
    assertThat(settingsRepository.findByWorkspaceId(workspace.workspaceId()))
        .get()
        .returns(Duration.ofDays(21), state -> state.settings().measurementStaleAfter())
        .returns(TitratableAcidityBasis.CITRIC_ACID, state -> state.settings().defaultAcidityBasis());
  }

  private @NonNull AccountUser persistUser(@NonNull String subjectSuffix) {
    return userRepository.create(AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_" + subjectSuffix),
        EmailAddress.of(subjectSuffix + "@example.com"),
        DisplayName.of("Test user " + subjectSuffix),
        null
    ));
  }

  private @NonNull Workspace persistWorkspace(@NonNull String name) {
    return workspaceRepository.create(Workspace.create(
        DisplayName.of(name),
        WorkspaceType.CIDERY
    ));
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan("org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account")
  static class AccountJdbcAdaptersTestConfiguration {
  }
}
