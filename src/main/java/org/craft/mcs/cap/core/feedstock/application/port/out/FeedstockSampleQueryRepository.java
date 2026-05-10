package org.craft.mcs.cap.core.feedstock.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.jspecify.annotations.NonNull;


public interface FeedstockSampleQueryRepository {

  @NonNull
  List<@NonNull FeedstockSample> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId);

  @NonNull
  List<@NonNull FeedstockSample> findAllByCiderMakerIdAndSeason(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockSeason season
  );

  @NonNull
  List<@NonNull FeedstockSample> findSugarAcidityPointCandidates(
      @NonNull FeedstockSampleSugarAcidityPointCandidateQuery query
  );

  @NonNull
  List<@NonNull FeedstockSampleSummary> findSummariesByBatchId(@NonNull FeedstockBatchId batchId);
}
