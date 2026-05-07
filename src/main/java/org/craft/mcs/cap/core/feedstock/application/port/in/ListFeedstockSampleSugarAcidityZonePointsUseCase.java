package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.jspecify.annotations.NonNull;


public interface ListFeedstockSampleSugarAcidityZonePointsUseCase {

  @NonNull
  List<@NonNull FeedstockSampleSugarAcidityZoneAssessment> listByCiderMakerId(
      @NonNull CiderMakerId ciderMakerId
  );
}
