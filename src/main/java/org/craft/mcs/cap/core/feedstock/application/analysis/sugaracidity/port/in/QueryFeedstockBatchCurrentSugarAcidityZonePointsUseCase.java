package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.jspecify.annotations.NonNull;


public interface QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase {

  @NonNull
  CursorPage<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> query(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  );
}
