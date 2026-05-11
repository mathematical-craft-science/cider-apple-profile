package org.craft.mcs.cap.core.feedstock.application.port.in;


public sealed interface FeedstockBatchBlendProfileTargetSelection
    permits PredefinedFeedstockBatchBlendProfileTargetSelection, CustomRectangleFeedstockBatchBlendProfileTargetSelection {
}
