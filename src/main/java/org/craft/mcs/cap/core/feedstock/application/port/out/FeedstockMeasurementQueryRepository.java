package org.craft.mcs.cap.core.feedstock.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.jspecify.annotations.NonNull;


public interface FeedstockMeasurementQueryRepository {

  @NonNull
  List<@NonNull FeedstockMeasurement> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId);

  @NonNull
  List<@NonNull FeedstockMeasurement> findAllByCiderMakerIdAndSeason(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockSeason season
  );

  @NonNull
  List<@NonNull FeedstockMeasurement> findSugarAcidityPointCandidates(
      @NonNull FeedstockMeasurementSugarAcidityPointCandidateQuery query
  );

  @NonNull
  List<@NonNull FeedstockMeasurement> findLatestSugarAcidityPointCandidates(
      @NonNull LatestFeedstockMeasurementSugarAcidityPointCandidateQuery query
  );

  @NonNull
  Optional<@NonNull FeedstockMeasurement> findLatestByBatchId(@NonNull FeedstockBatchId batchId);

  @NonNull
  List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(@NonNull FeedstockBatchId batchId);
}
