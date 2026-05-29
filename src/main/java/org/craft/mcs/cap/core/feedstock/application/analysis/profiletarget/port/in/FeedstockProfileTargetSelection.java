package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in;


public sealed interface FeedstockProfileTargetSelection
    permits PredefinedFeedstockProfileTargetSelection, CustomRectangleFeedstockProfileTargetSelection {
}
