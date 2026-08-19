package org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.jspecify.annotations.NonNull;


/** Supplies a page of state-aware current Feedstock profiles. */
public interface FeedstockCurrentProfilePageProvider {

  @NonNull
  List<@NonNull FeedstockCurrentProfileView> findProfiles(
      @NonNull FeedstockCurrentProfilePageQuery query
  );
}
