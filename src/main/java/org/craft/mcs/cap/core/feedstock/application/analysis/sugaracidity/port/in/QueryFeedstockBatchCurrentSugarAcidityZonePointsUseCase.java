package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;


public interface QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase {

  @NonNull
  CursorPage<
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult,
      @NonNull FeedstockMeasurementSugarAcidityPagePosition
  > query(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  );
}
