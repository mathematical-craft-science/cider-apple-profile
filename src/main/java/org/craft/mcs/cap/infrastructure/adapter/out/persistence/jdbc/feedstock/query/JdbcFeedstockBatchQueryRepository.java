package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/**
 * JDBC read-side adapter for feedstock batch summaries.
 */
@Repository
@RequiredArgsConstructor
class JdbcFeedstockBatchQueryRepository implements FeedstockBatchQueryRepository {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull List<@NonNull FeedstockBatchSummary> findSummariesByCiderMakerId(
      @NonNull CiderMakerId ciderMakerId
  ) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return jdbcTemplate.query("""
        SELECT batch_id,
               cider_maker_id,
               name,
               vessel,
               type,
               season_starts_on,
               season_ends_on,
               source,
               available_volume_liters,
               note
        FROM feedstock_batches
        WHERE cider_maker_id = :ciderMakerId
          AND deleted_at IS NULL
        ORDER BY created_at ASC, batch_id ASC
        """, Map.of("ciderMakerId", ciderMakerId.value()), JdbcFeedstockBatchQueryRepository::toSummary);
  }

  private static @NonNull FeedstockBatchSummary toSummary(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    return new FeedstockBatchSummary(
        FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
        CiderMakerId.of(resultSet.getObject("cider_maker_id", UUID.class)),
        FeedstockBatchName.of(resultSet.getString("name")),
        vessel(resultSet),
        FeedstockType.valueOf(resultSet.getString("type")),
        FeedstockSeason.of(
            resultSet.getDate("season_starts_on").toLocalDate(),
            resultSet.getDate("season_ends_on").toLocalDate()
        ),
        source(resultSet),
        VolumeValue.ofLiters(resultSet.getBigDecimal("available_volume_liters")),
        resultSet.getString("note")
    );
  }

  private static @Nullable FeedstockVessel vessel(@NonNull ResultSet resultSet)
      throws SQLException {
    final var value = resultSet.getString("vessel");
    return value == null ? null : FeedstockVessel.of(value);
  }

  private static @Nullable FeedstockSource source(@NonNull ResultSet resultSet)
      throws SQLException {
    final var value = resultSet.getString("source");
    return value == null ? null : FeedstockSource.of(value);
  }
}
