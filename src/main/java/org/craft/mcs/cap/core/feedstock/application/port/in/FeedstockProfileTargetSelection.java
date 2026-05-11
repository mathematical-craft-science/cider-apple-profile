package org.craft.mcs.cap.core.feedstock.application.port.in;


public sealed interface FeedstockProfileTargetSelection
    permits PredefinedFeedstockProfileTargetSelection, CustomRectangleFeedstockProfileTargetSelection {
}
