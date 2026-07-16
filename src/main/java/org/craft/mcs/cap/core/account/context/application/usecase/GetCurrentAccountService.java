package org.craft.mcs.cap.core.account.context.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.AccountMeView;
import org.craft.mcs.cap.core.account.context.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.UserPreferencesView;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.user.domain.model.PersonalAccountCapability.forActiveAccount;


/**
 * Reads the current CAP account after authentication-boundary provisioning.
 *
 * <p>This query never creates or synchronizes account state. Provisioning belongs exclusively to
 * Provisioning belongs exclusively to the authentication boundary. Workspace selection is resolved from explicit
 * request context, with an implicit fallback only for a single membership.
 */
@Component
@RequiredArgsConstructor
public final class GetCurrentAccountService
    implements GetCurrentAccountUseCase {

  private final @NonNull CurrentWorkspaceContextResolver currentWorkspaceContextResolver;
  private final @NonNull UserPreferencesRepository userPreferencesRepository;

  @Override
  public @NonNull AccountMeView get() {
    final var context = currentWorkspaceContextResolver.resolve();
    final var user = context.user();
    final var activeMembership = context.activeMembership();
    final var preferences = userPreferencesRepository.findByUserId(user.userId())
        .orElseGet(() -> UserPreferences.defaults(user.userId()));

    return new AccountMeView(
        AccountUserView.from(user),
        activeMembership.workspace(),
        context.memberships(),
        UserPreferencesView.from(preferences),
        forActiveAccount(),
        activeMembership.role().workspacePermissions()
    );
  }
}
