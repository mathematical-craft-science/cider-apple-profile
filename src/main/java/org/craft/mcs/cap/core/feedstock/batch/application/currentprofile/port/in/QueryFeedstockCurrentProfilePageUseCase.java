package org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageItem;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.jspecify.annotations.NonNull;

/** Publishes coherent current Feedstock profiles with the metadata required by read consumers. */
public interface QueryFeedstockCurrentProfilePageUseCase {

  @NonNull
  List<@NonNull FeedstockCurrentProfilePageItem> query(@NonNull FeedstockCurrentProfilePageQuery query);
}
