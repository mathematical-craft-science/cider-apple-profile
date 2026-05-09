package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.jspecify.annotations.NonNull;


public interface ListFeedstockSampleSugarAcidityZonePointsUseCase {

  @NonNull
  FeedstockSampleSugarAcidityZonePointBatch listByCiderMakerId(
      @NonNull CiderMakerId ciderMakerId
  );
}
