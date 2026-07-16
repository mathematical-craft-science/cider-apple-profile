package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateUserPreferencesUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.AccountPreferencesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.AccountUserResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.UpdateAccountPreferencesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.UpdateAccountProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.mapper.AccountUserRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Thin REST adapter for the current user's CAP-owned profile and preferences. */
@RestController
@RequiredArgsConstructor
public final class AccountUserController implements AccountUserApi {

  private final @NonNull GetCurrentAccountProfileUseCase getCurrentAccountProfileUseCase;
  private final @NonNull GetCurrentUserPreferencesUseCase getCurrentUserPreferencesUseCase;
  private final @NonNull UpdateAccountProfileUseCase updateAccountProfileUseCase;
  private final @NonNull UpdateUserPreferencesUseCase updateUserPreferencesUseCase;

  @Override
  @GetMapping(value = "/account/profile", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountUserResponse> profile() {
    return ResponseEntity.ok(AccountUserRestMapper.toResponse(getCurrentAccountProfileUseCase.get()));
  }

  @Override
  @PatchMapping(
      value = "/account/profile",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull AccountUserResponse> updateProfile(
      @RequestBody @NonNull UpdateAccountProfileRequest request
  ) {
    return ResponseEntity.ok(AccountUserRestMapper.toResponse(
        updateAccountProfileUseCase.update(AccountUserRestMapper.toCommand(request))
    ));
  }

  @Override
  @GetMapping(value = "/account/preferences", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountPreferencesResponse> preferences() {
    return ResponseEntity.ok(AccountUserRestMapper.toResponse(getCurrentUserPreferencesUseCase.get()));
  }

  @Override
  @PatchMapping(
      value = "/account/preferences",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull AccountPreferencesResponse> updatePreferences(
      @RequestBody @NonNull UpdateAccountPreferencesRequest request
  ) {
    return ResponseEntity.ok(AccountUserRestMapper.toResponse(
        updateUserPreferencesUseCase.update(AccountUserRestMapper.toCommand(request))
    ));
  }
}
