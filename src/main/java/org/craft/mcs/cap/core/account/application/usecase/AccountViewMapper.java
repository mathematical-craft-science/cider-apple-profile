package org.craft.mcs.cap.core.account.application.usecase;

import org.craft.mcs.cap.core.account.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.application.port.in.UserPreferencesView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceSettingsState;
import org.craft.mcs.cap.core.account.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.jspecify.annotations.NonNull;


/** Maps account aggregates to application read models without leaking persistence concerns. */
final class AccountViewMapper {

  private AccountViewMapper() {
  }

  static @NonNull AccountUserView toView(@NonNull AccountUser user) {
    return new AccountUserView(
        user.userId(),
        user.email(),
        user.displayName(),
        user.avatarUrl(),
        user.status()
    );
  }

  static @NonNull WorkspaceView toView(@NonNull Workspace workspace) {
    return new WorkspaceView(
        workspace.workspaceId(),
        workspace.name(),
        workspace.type()
    );
  }

  static @NonNull WorkspaceMembershipView toView(
      @NonNull WorkspaceMembership membership,
      @NonNull Workspace workspace
  ) {
    return new WorkspaceMembershipView(
        membership.membershipId(),
        toView(workspace),
        membership.role(),
        membership.status()
    );
  }

  static @NonNull UserPreferencesView toView(@NonNull UserPreferences preferences) {
    return new UserPreferencesView(preferences.locale(), preferences.timeZone());
  }

  static @NonNull WorkspaceSettingsView toView(@NonNull WorkspaceSettingsState state) {
    final var settings = state.settings();
    return new WorkspaceSettingsView(
        settings.workspaceId(),
        settings.measurementStaleAfter(),
        settings.defaultAcidityBasis(),
        state.revision(),
        state.createdAt(),
        state.updatedAt()
    );
  }
}
