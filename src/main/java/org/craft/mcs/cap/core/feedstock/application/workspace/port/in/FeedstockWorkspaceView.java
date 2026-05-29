package org.craft.mcs.cap.core.feedstock.application.workspace.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockWorkspaceView(
    @NonNull List<@NonNull FeedstockWorkspaceBatchView> batches,
    @Nullable FeedstockWorkspaceTargetAnalysisView targetAnalysis
) {

  public FeedstockWorkspaceView {
    batches = List.copyOf(requireNonNull(batches, "feedstock workspace batch views cannot be null"));
  }
}
