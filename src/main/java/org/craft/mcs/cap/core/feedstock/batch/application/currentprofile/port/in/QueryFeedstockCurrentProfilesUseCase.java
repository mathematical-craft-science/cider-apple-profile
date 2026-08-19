package org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.jspecify.annotations.NonNull;

/** Publishes current profiles coherent with supplied Feedstock batch snapshots. */
public interface QueryFeedstockCurrentProfilesUseCase {

  @NonNull
  List<@NonNull FeedstockCurrentProfileView> findCurrentProfiles(
      @NonNull List<@NonNull FeedstockBatchSummary> batches
  );
}
