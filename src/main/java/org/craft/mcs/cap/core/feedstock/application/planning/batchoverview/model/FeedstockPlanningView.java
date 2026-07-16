package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockPlanningView(
    @NonNull List<@NonNull FeedstockPlanningBatchView> batches,
    @Nullable FeedstockPlanningTargetAnalysisView targetAnalysis
) {

  public FeedstockPlanningView {
    batches = List.copyOf(requireNonNull(batches, "feedstock planning batch views cannot be null"));
  }
}
