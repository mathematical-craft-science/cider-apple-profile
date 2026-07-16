package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.mapper;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateAccountProfileCommand;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateUserPreferencesCommand;
import org.craft.mcs.cap.core.account.user.application.port.in.UserPreferencesView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.AccountPreferencesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.AccountUserResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.UpdateAccountPreferencesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.UpdateAccountProfileRequest;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

/** Maps CAP-owned user profile and preference contracts. */
public final class AccountUserRestMapper {

  private AccountUserRestMapper() {
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

  public static @NonNull AccountPreferencesResponse toResponse(@NonNull UserPreferencesView view) {
    return new AccountPreferencesResponse(view.locale().toLanguageTag(), view.timeZone().getId());
  }

  public static @NonNull UpdateAccountProfileCommand toCommand(
      @NonNull UpdateAccountProfileRequest request
  ) {
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
