package org.craft.mcs.cap.infrastructure.adapter.out.settings.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSettings;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileSettingsProvider;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Maps Account-owned workspace settings to the Feedstock current-profile settings port. */
@Component
@RequiredArgsConstructor
public final class AccountFeedstockCurrentProfileSettingsProvider
    implements FeedstockCurrentProfileSettingsProvider {

  private final @NonNull WorkspaceSettingsReader workspaceSettingsReader;

  @Override
  public @NonNull FeedstockCurrentProfileSettings get(@NonNull CiderMakerId ciderMakerId) {
    final var settings = workspaceSettingsReader.readByCiderMakerId(ciderMakerId);
    return new FeedstockCurrentProfileSettings(settings.measurementStaleAfter());
  }
}
