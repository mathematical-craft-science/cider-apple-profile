package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.PhoneNumber;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsState;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.workspace.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import static java.util.Objects.requireNonNull;

/** Maps account aggregates to Spring Data JDBC entities while preserving persistence state on updates. */
public final class AccountJdbcMapper {

  private AccountJdbcMapper() {
  }

  public static @NonNull AccountUserJdbcEntity fromDomain(@NonNull AccountUser user) {
    return new AccountUserJdbcEntity(
        user.userId().value(),
        user.identityProvider().value(),
        user.externalSubject().value(),
        user.email().value(),
        user.displayName().value(),
        user.avatarUrl() == null ? null : user.avatarUrl().value().toString(),
        user.status()
    );
  }

  public static @NonNull AccountUserJdbcEntity updateFromDomain(
      @NonNull AccountUser user,
      @NonNull AccountUserJdbcEntity entity
  ) {
    entity.identityProvider = user.identityProvider().value();
    entity.externalSubject = user.externalSubject().value();
    entity.email = user.email().value();
    entity.displayName = user.displayName().value();
    entity.avatarUrl = user.avatarUrl() == null ? null : user.avatarUrl().value().toString();
    entity.status = user.status();
    return entity;
  }

  public static @NonNull AccountUser toDomain(@NonNull AccountUserJdbcEntity entity) {
    return new AccountUser(
        AccountUserId.of(entity.userId),
        IdentityProviderCode.of(entity.identityProvider),
        ExternalUserSubject.of(entity.externalSubject),
        EmailAddress.of(entity.email),
        DisplayName.of(entity.displayName),
        HttpUrl.fromNullable(entity.avatarUrl),
        entity.status
    );
  }

  public static @NonNull WorkspaceJdbcEntity fromDomain(@NonNull Workspace workspace) {
    return new WorkspaceJdbcEntity(
        workspace.workspaceId().value(),
        workspace.name().value(),
        workspace.type(),
        workspace.status(),
        workspace.externalOrganizationProvider() == null
            ? null
            : workspace.externalOrganizationProvider().value(),
        workspace.externalOrganizationSubject() == null
            ? null
            : workspace.externalOrganizationSubject().value()
    );
  }

  public static @NonNull WorkspaceJdbcEntity updateFromDomain(
      @NonNull Workspace workspace,
      @NonNull WorkspaceJdbcEntity entity
  ) {
    entity.name = workspace.name().value();
    entity.type = workspace.type();
    entity.status = workspace.status();
    entity.externalOrganizationProvider = workspace.externalOrganizationProvider() == null
        ? null
        : workspace.externalOrganizationProvider().value();
    entity.externalOrganizationSubject = workspace.externalOrganizationSubject() == null
        ? null
        : workspace.externalOrganizationSubject().value();
    return entity;
  }

  public static @NonNull Workspace toDomain(@NonNull WorkspaceJdbcEntity entity) {
    return new Workspace(
        WorkspaceId.of(entity.workspaceId),
        DisplayName.of(entity.name),
        entity.type,
        entity.status,
        entity.externalOrganizationProvider == null
            ? null
            : IdentityProviderCode.of(entity.externalOrganizationProvider),
        entity.externalOrganizationSubject == null
            ? null
            : ExternalOrganizationSubject.of(entity.externalOrganizationSubject)
    );
  }

  public static @NonNull WorkspaceMembershipJdbcEntity fromDomain(
      @NonNull WorkspaceMembership membership
  ) {
    return new WorkspaceMembershipJdbcEntity(
        membership.membershipId().value(),
        AggregateReference.to(membership.workspaceId().value()),
        AggregateReference.to(membership.userId().value()),
        membership.role(),
        membership.status()
    );
  }

  public static @NonNull WorkspaceMembershipJdbcEntity updateFromDomain(
      @NonNull WorkspaceMembership membership,
      @NonNull WorkspaceMembershipJdbcEntity entity
  ) {
    entity.workspaceId = AggregateReference.to(membership.workspaceId().value());
    entity.userId = AggregateReference.to(membership.userId().value());
    entity.role = membership.role();
    entity.status = membership.status();
    return entity;
  }

  public static @NonNull WorkspaceMembership toDomain(@NonNull WorkspaceMembershipJdbcEntity entity) {
    return new WorkspaceMembership(
        WorkspaceMembershipId.of(entity.membershipId),
        WorkspaceId.of(entity.workspaceId.getId()),
        AccountUserId.of(entity.userId.getId()),
        entity.role,
        entity.status
    );
  }

  public static @NonNull WorkspaceSettingsJdbcEntity fromDomain(@NonNull WorkspaceSettings settings) {
    return new WorkspaceSettingsJdbcEntity(
        settings.workspaceId().value(),
        settings.measurementStaleAfter().toSeconds(),
        settings.defaultAcidityBasis()
    );
  }

  public static @NonNull WorkspaceSettingsJdbcEntity updateFromDomain(
      @NonNull WorkspaceSettings settings,
      @NonNull WorkspaceSettingsJdbcEntity entity
  ) {
    entity.measurementStaleAfterSeconds = settings.measurementStaleAfter().toSeconds();
    entity.defaultAcidityBasis = settings.defaultAcidityBasis();
    return entity;
  }

  public static @NonNull WorkspaceSettings toDomain(@NonNull WorkspaceSettingsJdbcEntity entity) {
    return new WorkspaceSettings(
        WorkspaceId.of(entity.workspaceId),
        Duration.ofSeconds(entity.measurementStaleAfterSeconds),
        entity.defaultAcidityBasis
    );
  }

  public static @NonNull WorkspaceSettingsState toState(@NonNull WorkspaceSettingsJdbcEntity entity) {
    return new WorkspaceSettingsState(
        toDomain(entity),
        WorkspaceSettingsRevision.of(entity.version),
        requireNonNull(entity.createdAt, "workspace settings creation time cannot be null"),
        requireNonNull(entity.updatedAt, "workspace settings update time cannot be null")
    );
  }

  public static @NonNull UserPreferencesJdbcEntity fromDomain(@NonNull UserPreferences preferences) {
    return new UserPreferencesJdbcEntity(
        preferences.userId().value(),
        preferences.locale().toLanguageTag(),
        preferences.timeZone().getId()
    );
  }

  public static @NonNull UserPreferencesJdbcEntity updateFromDomain(
      @NonNull UserPreferences preferences,
      @NonNull UserPreferencesJdbcEntity entity
  ) {
    entity.locale = preferences.locale().toLanguageTag();
    entity.timeZone = preferences.timeZone().getId();
    return entity;
  }

  public static @NonNull UserPreferences toDomain(@NonNull UserPreferencesJdbcEntity entity) {
    return new UserPreferences(
        AccountUserId.of(entity.userId),
        Locale.forLanguageTag(entity.locale),
        ZoneId.of(entity.timeZone)
    );
  }

  public static @NonNull ContributorProfileJdbcEntity fromDomain(@NonNull ContributorProfile profile) {
    return new ContributorProfileJdbcEntity(
        profile.userId().value(),
        profile.contributorType(),
        profile.publicName().value(),
        profile.website() == null ? null : profile.website().value().toString(),
        profile.publicEmail() == null ? null : profile.publicEmail().value(),
        profile.publicPhone() == null ? null : profile.publicPhone().value(),
        profile.contactVisibility(),
        profile.contactPublicationConsentedAt()
    );
  }

  public static @NonNull ContributorProfileJdbcEntity updateFromDomain(
      @NonNull ContributorProfile profile,
      @NonNull ContributorProfileJdbcEntity entity
  ) {
    entity.contributorType = profile.contributorType();
    entity.publicName = profile.publicName().value();
    entity.website = profile.website() == null ? null : profile.website().value().toString();
    entity.publicEmail = profile.publicEmail() == null ? null : profile.publicEmail().value();
    entity.publicPhone = profile.publicPhone() == null ? null : profile.publicPhone().value();
    entity.contactVisibility = profile.contactVisibility();
    entity.contactPublicationConsentedAt = profile.contactPublicationConsentedAt();
    return entity;
  }

  public static @NonNull ContributorProfile toDomain(@NonNull ContributorProfileJdbcEntity entity) {
    return new ContributorProfile(
        AccountUserId.of(entity.userId),
        entity.contributorType,
        DisplayName.of(entity.publicName),
        HttpUrl.fromNullable(entity.website),
        EmailAddress.fromNullable(entity.publicEmail),
        PhoneNumber.fromNullable(entity.publicPhone),
        entity.contactVisibility,
        entity.contactPublicationConsentedAt
    );
  }
}
