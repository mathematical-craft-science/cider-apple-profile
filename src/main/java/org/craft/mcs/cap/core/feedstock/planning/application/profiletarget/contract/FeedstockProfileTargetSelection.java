package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract;


public sealed interface FeedstockProfileTargetSelection
    permits PredefinedFeedstockProfileTargetSelection, CustomRectangleFeedstockProfileTargetSelection {
}
