package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementSugarAcidityPointCandidateCursor;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.entity.FeedstockMeasurementJdbcMapper;
import org.craft.mcs.cap.shared.platform.persistence.jdbc.SqlQueryBuilder;
import org.craft.mcs.cap.shared.platform.persistence.jdbc.SqlQueryBuilder.SqlQueryOption;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JdbcFeedstockMeasurementQueryRepository
    implements FeedstockMeasurementQueryRepository {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  private static @NonNull SqlQueryBuilder baseFeedstockMeasurementQuery(@NonNull CiderMakerId ciderMakerId) {
    return SqlQueryBuilder.from("""
        select sample.*
        from feedstock_measurements sample
        join feedstock_batches batch on sample.batch_id = batch.batch_id
        where batch.cider_maker_id = :ciderMakerId
          and batch.deleted_at is null
        """).parameter("ciderMakerId", ciderMakerId.value());
  }

  private static @NonNull SqlQueryOption batchFilter(@Nullable FeedstockBatchCriteria batch) {
    return builder -> {
      if (batch == null) {
        return;
      }

      builder.where("sample.batch_id in (:batchIds)");
      builder.parameter("batchIds", batch.batchIds().stream()
          .map(FeedstockBatchId::value)
          .toList());
    };
  }

  private static @NonNull SqlQueryOption seasonFilter(@Nullable FeedstockSeason season) {
    return builder -> {
      if (season == null) {
        return;
      }

      builder
          .where("batch.season_starts_on <= :seasonEndsOn")
          .where("batch.season_ends_on >= :seasonStartsOn")
          .parameter("seasonStartsOn", season.startsOn())
          .parameter("seasonEndsOn", season.endsOn());
    };
  }

  private static @NonNull SqlQueryOption afterCursor(
      @Nullable FeedstockMeasurementSugarAcidityPointCandidateCursor cursor,
      @NonNull CursorDirection direction
  ) {
    return builder -> {
      if (cursor == null) {
        return;
      }

      builder
          .where("""
              sample.measured_at %s :afterMeasuredAt
                or (sample.measured_at = :afterMeasuredAt and sample.measurement_id %s :afterSampleId)
              """.formatted(direction.operator(), direction.operator()))
          .parameter("afterMeasuredAt", timestamp(cursor.measuredAt()))
          .parameter("afterSampleId", cursor.measurementId());
    };
  }

  private static @NonNull SqlQueryOption latestMeasurementOnly() {
    return builder -> builder.where("""
        not exists (
          select newer.measurement_id
          from feedstock_measurements newer
          where newer.batch_id = sample.batch_id
            and (newer.measured_at > sample.measured_at
              or (newer.measured_at = sample.measured_at and newer.measurement_id > sample.measurement_id))
        )
        """);
  }

  private static @NonNull OffsetDateTime timestamp(@NonNull Instant value) {
    return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurement> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return queryMeasurements("""
        select sample.*
        from feedstock_measurements sample
        where sample.batch_id in (
          select batch.batch_id
          from feedstock_batches batch
          where batch.cider_maker_id = :ciderMakerId
            and batch.deleted_at is null
        )
        """, Map.of("ciderMakerId", ciderMakerId.value()));
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurement> findAllByCiderMakerIdAndSeason(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockSeason season
  ) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(season, "feedstock season cannot be null");

    return queryMeasurements("""
        select sample.*
        from feedstock_measurements sample
        join feedstock_batches batch on sample.batch_id = batch.batch_id
        where batch.cider_maker_id = :ciderMakerId
          and batch.deleted_at is null
          and batch.season_starts_on <= :seasonEndsOn
          and batch.season_ends_on >= :seasonStartsOn
        """, Map.of(
        "ciderMakerId", ciderMakerId.value(),
        "seasonStartsOn", season.startsOn(),
        "seasonEndsOn", season.endsOn()
    ));
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurement> findSugarAcidityPointCandidates(
      @NonNull FeedstockMeasurementSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "feedstock measurement point candidate query cannot be null");

    final var sqlQuery = baseFeedstockMeasurementQuery(query.ciderMakerId())
        .with(batchFilter(query.batch()))
        .with(seasonFilter(query.season()))
        .with(afterCursor(query.after(), CursorDirection.ASCENDING))
        .orderBy("sample.measured_at asc, sample.measurement_id asc")
        .limit(query.limit())
        .build();

    return queryMeasurements(sqlQuery.sql(), sqlQuery.parameters());
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurement> findLatestSugarAcidityPointCandidates(
      @NonNull LatestFeedstockMeasurementSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "latest feedstock measurement point candidate query cannot be null");

    final var sqlQuery = baseFeedstockMeasurementQuery(query.ciderMakerId())
        .with(batchFilter(query.batch()))
        .with(seasonFilter(query.season()))
        .with(afterCursor(query.after(), CursorDirection.DESCENDING))
        .with(latestMeasurementOnly())
        .orderBy("sample.measured_at desc, sample.measurement_id desc")
        .limit(query.limit())
        .build();

    return queryMeasurements(sqlQuery.sql(), sqlQuery.parameters());
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurement> findLatestByBatchId(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return queryMeasurements("""
        select *
        from feedstock_measurements
        where batch_id = :batchId
        order by measured_at desc, measurement_id desc
        limit 1
        """, Map.of("batchId", batchId.value()))
        .stream()
        .findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return jdbcTemplate.query("""
        SELECT batch.cider_maker_id, sample.*
        FROM feedstock_measurements sample
        JOIN feedstock_batches batch ON sample.batch_id = batch.batch_id
        WHERE sample.batch_id = :batchId
          AND batch.deleted_at IS NULL
        ORDER BY sample.measured_at DESC, sample.name ASC
        """, Map.of("batchId", batchId.value()), (resultSet, rowNumber) -> {
      final var measurement = FeedstockMeasurementJdbcMapper.toDomain(
          FeedstockMeasurementJdbcMapper.fromResultSet(resultSet)
      );
      return new FeedstockMeasurementSummary(
          measurement.measurementId(),
          CiderMakerId.of(resultSet.getObject("cider_maker_id", UUID.class)),
          measurement.batchId(),
          measurement.name(),
          measurement.measuredAt(),
          measurement.chemicalProfile(),
          measurement.note()
      );
    });
  }

  private @NonNull List<@NonNull FeedstockMeasurement> queryMeasurements(
      @NonNull String sql,
      @NonNull Map<String, ?> parameters
  ) {
    return queryMeasurements(sql, new MapSqlParameterSource(parameters));
  }

  private @NonNull List<@NonNull FeedstockMeasurement> queryMeasurements(
      @NonNull String sql,
      @NonNull SqlParameterSource parameters
  ) {
    return jdbcTemplate.query(
        sql,
        parameters,
        (resultSet, rowNumber) -> FeedstockMeasurementJdbcMapper.toDomain(
            FeedstockMeasurementJdbcMapper.fromResultSet(resultSet)
        )
    );
  }

  private enum CursorDirection {
    ASCENDING(">"),
    DESCENDING("<");

    private final @NonNull String operator;

    CursorDirection(@NonNull String operator) {
      this.operator = operator;
    }

    private @NonNull String operator() {
      return operator;
    }
  }
}
