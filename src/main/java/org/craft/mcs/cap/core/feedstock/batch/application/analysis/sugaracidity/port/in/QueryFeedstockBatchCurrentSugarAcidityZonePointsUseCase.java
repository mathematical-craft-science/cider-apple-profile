package org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;


public interface QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase {

  @NonNull
  CursorPage<
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult,
      @NonNull FeedstockCurrentProfilePagePosition
  > query(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  );
}
