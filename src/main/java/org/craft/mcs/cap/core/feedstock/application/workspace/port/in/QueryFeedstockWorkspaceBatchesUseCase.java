package org.craft.mcs.cap.core.feedstock.application.workspace.port.in;

import org.jspecify.annotations.NonNull;


public interface QueryFeedstockWorkspaceBatchesUseCase {

  @NonNull
  FeedstockWorkspaceView query(@NonNull FeedstockWorkspaceBatchViewQuery query);
}
