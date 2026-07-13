package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.domain.model.ContributorType;
import org.craft.mcs.cap.core.account.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.domain.model.value.PhoneNumber;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;


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
    PostgresAccountProvisioningLock.class
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
    final var settings = settingsRepository.save(new WorkspaceSettings(
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
        ContactVisibility.PUBLIC
    ));

    assertThat(userRepository.findByExternalIdentity(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123")
    )).contains(user);
    assertThat(workspaceRepository.findByCiderMakerId(workspace.ciderMakerId())).contains(workspace);
    assertThat(membershipRepository.findByWorkspaceIdAndUserId(workspace.workspaceId(), user.userId()))
        .contains(membership);
    assertThat(membershipRepository.findAllByUserId(user.userId())).containsExactly(membership);
    assertThat(settingsRepository.findByWorkspaceId(workspace.workspaceId())).contains(settings);
    assertThat(preferencesRepository.findByUserId(user.userId())).contains(preferences);
    assertThat(contributorProfileRepository.findByUserId(user.userId())).contains(contributorProfile);
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
}
