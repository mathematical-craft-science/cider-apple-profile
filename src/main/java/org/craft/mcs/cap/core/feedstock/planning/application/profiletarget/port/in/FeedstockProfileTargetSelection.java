package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;


public sealed interface FeedstockProfileTargetSelection
    permits PredefinedFeedstockProfileTargetSelection, CustomRectangleFeedstockProfileTargetSelection {
}
