package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.application.port.in.AccountMeView;
import org.craft.mcs.cap.core.account.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.application.port.in.UpdateAccountProfileCommand;
import org.craft.mcs.cap.core.account.application.port.in.UpdateUserPreferencesCommand;
import org.craft.mcs.cap.core.account.application.port.in.UserPreferencesView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMemberView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountMeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountPreferencesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountUserResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceMemberResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateAccountPreferencesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateAccountProfileRequest;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;


/** Maps CAP account application views and commands to the provider-neutral REST contract. */
public final class AccountRestMapper {

  private AccountRestMapper() {
  }

  public static @NonNull AccountMeResponse toResponse(@NonNull AccountMeView view) {
    return new AccountMeResponse(
        toResponse(view.user()),
        toResponse(view.activeWorkspace()),
        view.memberships().stream().map(AccountRestMapper::toResponse).toList(),
        toResponse(view.preferences()),
        view.permissions()
    );
  }

  public static @NonNull AccountUserResponse toResponse(@NonNull AccountUserView view) {
    return new AccountUserResponse(
        view.userId().value(),
        view.email().value(),
        view.displayName().value(),
        view.avatarUrl() == null ? null : view.avatarUrl().value().toString(), // NOSONAR
        view.status()
    );
  }

  public static @NonNull AccountWorkspaceResponse toResponse(@NonNull WorkspaceView view) {
    return new AccountWorkspaceResponse(
        view.workspaceId().value(),
        view.name().value(),
        view.type()
    );
  }

  public static @NonNull AccountMembershipResponse toResponse(@NonNull WorkspaceMembershipView view) {
    return new AccountMembershipResponse(
        view.membershipId().value(),
        toResponse(view.workspace()),
        view.role(),
        view.status()
    );
  }

  public static @NonNull AccountPreferencesResponse toResponse(@NonNull UserPreferencesView view) {
    return new AccountPreferencesResponse(view.locale().toLanguageTag(), view.timeZone().getId());
  }

  public static @NonNull AccountWorkspaceMemberResponse toResponse(@NonNull WorkspaceMemberView view) {
    return new AccountWorkspaceMemberResponse(
        view.membershipId().value(),
        toResponse(view.user()),
        view.role(),
        view.status()
    );
  }

  public static @NonNull UpdateAccountProfileCommand toCommand(@NonNull UpdateAccountProfileRequest request) {
    return new UpdateAccountProfileCommand(
        DisplayName.of(request.displayName()),
        HttpUrl.fromNullable(request.avatarUrl())
    );
  }

  public static @NonNull UpdateUserPreferencesCommand toCommand(
      @NonNull UpdateAccountPreferencesRequest request
  ) {
    final var locale = Locale.forLanguageTag(request.locale());
    if (request.locale().isBlank() || locale.getLanguage().isBlank()) {
      throw new ApplicationValidationException("locale must be a valid BCP 47 language tag");
    }

    try {
      return new UpdateUserPreferencesCommand(locale, ZoneId.of(request.timeZone()));
    } catch (DateTimeException _) {
      throw new ApplicationValidationException("timeZone must be a valid IANA time-zone id");
    }
  }
}
