package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.jspecify.annotations.NonNull;


public interface QueryFeedstockSampleSugarAcidityZonePointsUseCase {

  @NonNull
  CursorPage<@NonNull FeedstockSampleSugarAcidityZoneResult> query(
      @NonNull FeedstockSampleSugarAcidityZonePointQuery query
  );
}
