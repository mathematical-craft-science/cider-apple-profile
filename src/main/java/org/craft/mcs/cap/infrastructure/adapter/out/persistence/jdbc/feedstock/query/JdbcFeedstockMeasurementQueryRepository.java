package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** JDBC adapter for Feedstock measurement history and current-profile queries. */
@Repository
@RequiredArgsConstructor
class JdbcFeedstockMeasurementQueryRepository implements FeedstockMeasurementQueryRepository {

  private final @NonNull JdbcFeedstockMeasurementSetQuery measurementSetQuery;

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurementView> findById(
      @NonNull FeedstockMeasurementId measurementId
  ) {
    return measurementSetQuery.findById(
        requireNonNull(measurementId, "feedstock measurement id cannot be null")
    );
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    return measurementSetQuery.findSummariesByBatchId(
        requireNonNull(batchId, "feedstock batch id cannot be null")
    );
  }
}
