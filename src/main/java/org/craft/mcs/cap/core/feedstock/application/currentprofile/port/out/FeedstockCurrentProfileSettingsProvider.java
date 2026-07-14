package org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out;

import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSettings;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Supplies effective workspace settings without exposing Account concepts to Feedstock. */
public interface FeedstockCurrentProfileSettingsProvider {

  @NonNull FeedstockCurrentProfileSettings get(@NonNull WorkspaceId workspaceId);
}
