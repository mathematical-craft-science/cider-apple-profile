package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockProfileAxisEvidence;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockProfileCharacteristic;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.core.measurement.domain.projection.MeasurementChemicalProfileProjector;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement.MeasurementJdbcReadSupport;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/**
 * Feedstock read adapter over generic measurement evidence.
 *
 * <p>Historical queries project one active {@code MeasurementSet}. Current queries select the
 * latest active RESULT independently for each characteristic, so a partial measurement does not
 * hide older evidence for other characteristics.
 */
@Repository
@RequiredArgsConstructor
class JdbcFeedstockMeasurementQueryRepository
    implements FeedstockMeasurementQueryRepository {

  private static final Set<MeasurementCharacteristic> CHEMICAL_CHARACTERISTICS = Set.of(
      MeasurementCharacteristic.SPECIFIC_GRAVITY,
      MeasurementCharacteristic.PH,
      MeasurementCharacteristic.TITRATABLE_ACIDITY,
      MeasurementCharacteristic.TANNIN,
      MeasurementCharacteristic.YAN
  );

  private static final String SET_CANDIDATES = """
      SELECT s.measurement_set_id,
             s.workspace_id,
             s.subject_id AS batch_id,
             revision.name,
             revision.note,
             s.version,
             MAX(record.measured_at) AS measured_at
        FROM measurement_sets s
        JOIN measurement_set_revisions revision
          ON revision.revision_id = s.current_revision_id
        JOIN measurement_records record
          ON record.revision_id = revision.revision_id
         AND record.role = 'RESULT'
        JOIN feedstock_batches batch
          ON batch.batch_id = s.subject_id
         AND batch.deleted_at IS NULL
       WHERE s.subject_type = 'FEEDSTOCK_BATCH'
         AND s.lifecycle = 'ACTIVE'
         AND record.characteristic IN (
             'SPECIFIC_GRAVITY',
             'PH',
             'TITRATABLE_ACIDITY',
             'TANNIN',
             'YAN'
         )
      """;

  private static final String CURRENT_RECORDS = """
      WITH ranked_records AS (
          SELECT s.measurement_set_id,
                 s.workspace_id,
                 s.subject_id AS batch_id,
                 revision.name,
                 revision.note,
                 s.version,
                 record.*,
                 ROW_NUMBER() OVER (
                     PARTITION BY s.subject_id, record.characteristic
                     ORDER BY record.measured_at DESC,
                              record.record_id DESC,
                              s.measurement_set_id DESC
                 ) AS characteristic_rank
            FROM measurement_sets s
            JOIN measurement_set_revisions revision
              ON revision.revision_id = s.current_revision_id
            JOIN measurement_records record
              ON record.revision_id = revision.revision_id
             AND record.role = 'RESULT'
           WHERE s.subject_type = 'FEEDSTOCK_BATCH'
             AND s.lifecycle = 'ACTIVE'
             AND s.subject_id IN (:batchIds)
             AND record.characteristic IN (
                 'SPECIFIC_GRAVITY',
                 'PH',
                 'TITRATABLE_ACIDITY',
                 'TANNIN',
                 'YAN'
             )
      )
      SELECT *
        FROM ranked_records
       WHERE characteristic_rank = 1
       ORDER BY batch_id, characteristic
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurementView> findById(
      @NonNull FeedstockMeasurementId measurementId
  ) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    final var candidates = jdbcTemplate.query(
        SET_CANDIDATES
            + """
               AND s.measurement_set_id = :measurementSetId
               GROUP BY s.measurement_set_id,
                        s.workspace_id,
                        s.subject_id,
                        revision.name,
                        revision.note,
                        s.version
              """,
        new MapSqlParameterSource("measurementSetId", measurementId.value()),
        JdbcFeedstockMeasurementQueryRepository::mapCandidate
    );
    return loadSetViews(candidates).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementView> findSugarAcidityPointCandidates(
      @NonNull FeedstockMeasurementSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "feedstock measurement point candidate query cannot be null");
    final var candidates = querySetCandidates(
        query.workspaceId(),
        query.batch(),
        query.season(),
        query.after(),
        CursorDirection.ASCENDING,
        query.limit()
    );
    return loadSetViews(candidates);
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementView> findLatestSugarAcidityPointCandidates(
      @NonNull LatestFeedstockMeasurementSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "latest feedstock measurement point candidate query cannot be null");
    final var batchIds = queryCurrentBatchIds(
        query.workspaceId(),
        query.batch(),
        query.season(),
        query.after(),
        CursorDirection.DESCENDING,
        query.limit()
    );
    return loadCurrentViews(batchIds);
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurementView> findLatestByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return loadCurrentViews(List.of(batchId)).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementView> findLatestByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    return loadCurrentViews(List.copyOf(batchIds));
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    final var candidates = querySetCandidates(
        null,
        FeedstockBatchCriteria.of(batchId),
        null,
        null,
        CursorDirection.DESCENDING,
        Integer.MAX_VALUE
    );
    return loadSetViews(candidates).stream()
        .map(view -> new FeedstockMeasurementSummary(
            view.measurementId(),
            workspaceId(candidates, view.measurementId()),
            view.batchId(),
            view.name(),
            view.measuredAt(),
            view.chemicalProfile(),
            view.note()
        ))
        .toList();
  }

  private @NonNull List<@NonNull SetCandidate> querySetCandidates(
      @Nullable WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season,
      @Nullable FeedstockMeasurementSugarAcidityPagePosition cursor,
      @NonNull CursorDirection direction,
      int limit
  ) {
    final var sql = new StringBuilder(SET_CANDIDATES);
    final var parameters = new MapSqlParameterSource();
    addFilters(sql, parameters, workspaceId, batchCriteria, season);
    sql.append("""
         GROUP BY s.measurement_set_id,
                  s.workspace_id,
                  s.subject_id,
                  revision.name,
                  revision.note,
                  s.version
        """);
    if (cursor != null) {
      sql.append("""
           HAVING MAX(record.measured_at) %s :afterMeasuredAt
               OR (
                   MAX(record.measured_at) = :afterMeasuredAt
                   AND s.measurement_set_id %s :afterMeasurementSetId
               )
          """.formatted(direction.operator(), direction.operator()));
      parameters
          .addValue("afterMeasuredAt", cursor.measuredAt())
          .addValue("afterMeasurementSetId", cursor.measurementId());
    }
    sql.append(" ORDER BY measured_at ")
        .append(direction.order())
        .append(", s.measurement_set_id ")
        .append(direction.order());
    if (limit != Integer.MAX_VALUE) {
      sql.append(" LIMIT :limit");
      parameters.addValue("limit", limit);
    }
    return jdbcTemplate.query(sql.toString(), parameters, JdbcFeedstockMeasurementQueryRepository::mapCandidate);
  }

  private @NonNull List<@NonNull FeedstockBatchId> queryCurrentBatchIds(
      @NonNull WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season,
      @Nullable FeedstockMeasurementSugarAcidityPagePosition cursor,
      @NonNull CursorDirection direction,
      int limit
  ) {
    final var sql = new StringBuilder("""
        WITH ranked_records AS (
            SELECT s.subject_id AS batch_id,
                   s.measurement_set_id,
                   record.record_id,
                   record.characteristic,
                   record.measured_at,
                   ROW_NUMBER() OVER (
                       PARTITION BY s.subject_id, record.characteristic
                       ORDER BY record.measured_at DESC,
                                record.record_id DESC,
                                s.measurement_set_id DESC
                   ) AS characteristic_rank
              FROM measurement_sets s
              JOIN measurement_set_revisions revision
                ON revision.revision_id = s.current_revision_id
              JOIN measurement_records record
                ON record.revision_id = revision.revision_id
               AND record.role = 'RESULT'
              JOIN feedstock_batches batch
                ON batch.batch_id = s.subject_id
               AND batch.deleted_at IS NULL
             WHERE s.subject_type = 'FEEDSTOCK_BATCH'
               AND s.lifecycle = 'ACTIVE'
               AND record.characteristic IN (
                   'SPECIFIC_GRAVITY',
                   'PH',
                   'TITRATABLE_ACIDITY',
                   'TANNIN',
                   'YAN'
               )
        """);
    final var parameters = new MapSqlParameterSource();
    addFilters(sql, parameters, workspaceId, batchCriteria, season);
    sql.append("""
        ),
        current_records AS (
            SELECT *
              FROM ranked_records
             WHERE characteristic_rank = 1
        ),
        current_batches AS (
            SELECT batch_id,
                   MAX(measured_at) AS measured_at,
                   (ARRAY_AGG(
                       measurement_set_id
                       ORDER BY measured_at DESC, record_id DESC, measurement_set_id DESC
                   ))[1] AS anchor_set_id
              FROM current_records
             GROUP BY batch_id
        )
        SELECT batch_id
          FROM current_batches
         WHERE 1 = 1
        """);
    if (cursor != null) {
      sql.append("""
           AND (
               measured_at %s :afterMeasuredAt
               OR (
                   measured_at = :afterMeasuredAt
                   AND anchor_set_id %s :afterMeasurementSetId
               )
           )
          """.formatted(direction.operator(), direction.operator()));
      parameters
          .addValue("afterMeasuredAt", cursor.measuredAt())
          .addValue("afterMeasurementSetId", cursor.measurementId());
    }
    sql.append(" ORDER BY measured_at ")
        .append(direction.order())
        .append(", anchor_set_id ")
        .append(direction.order())
        .append(" LIMIT :limit");
    parameters.addValue("limit", limit);
    return jdbcTemplate.query(
        sql.toString(),
        parameters,
        (resultSet, rowNumber) -> FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class))
    );
  }

  private static void addFilters(
      @NonNull StringBuilder sql,
      @NonNull MapSqlParameterSource parameters,
      @Nullable WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season
  ) {
    if (workspaceId != null) {
      sql.append(" AND s.workspace_id = :workspaceId");
      parameters.addValue("workspaceId", workspaceId.value());
    }
    if (batchCriteria != null) {
      sql.append(" AND s.subject_id IN (:batchIds)");
      parameters.addValue(
          "batchIds",
          batchCriteria.batchIds().stream().map(FeedstockBatchId::value).toList()
      );
    }
    if (season != null) {
      sql.append("""
           AND batch.season_starts_on <= :seasonEndsOn
           AND batch.season_ends_on >= :seasonStartsOn
          """);
      parameters
          .addValue("seasonStartsOn", season.startsOn())
          .addValue("seasonEndsOn", season.endsOn());
    }
  }

  private @NonNull List<@NonNull FeedstockMeasurementView> loadSetViews(
      @NonNull List<@NonNull SetCandidate> candidates
  ) {
    if (candidates.isEmpty()) {
      return List.of();
    }
    final var records = loadRecordsBySetIds(candidates.stream().map(SetCandidate::measurementSetId).toList());
    return candidates.stream()
        .map(candidate -> toView(candidate, records.getOrDefault(candidate.measurementSetId(), List.of())))
        .flatMap(Optional::stream)
        .toList();
  }

  private @NonNull List<@NonNull FeedstockMeasurementView> loadCurrentViews(
      @NonNull List<@NonNull FeedstockBatchId> batchIds
  ) {
    if (batchIds.isEmpty()) {
      return List.of();
    }
    final var rows = jdbcTemplate.query(
        CURRENT_RECORDS,
        new MapSqlParameterSource(
            "batchIds",
            batchIds.stream().map(FeedstockBatchId::value).toList()
        ),
        JdbcFeedstockMeasurementQueryRepository::mapRecord
    );
    final Map<FeedstockBatchId, List<RecordRow>> recordsByBatch = new LinkedHashMap<>();
    rows.forEach(row -> recordsByBatch
        .computeIfAbsent(row.batchId(), _ -> new ArrayList<>())
        .add(row));

    final Map<FeedstockBatchId, Integer> order = new LinkedHashMap<>();
    for (var index = 0; index < batchIds.size(); index++) {
      order.put(batchIds.get(index), index);
    }
    return recordsByBatch.entrySet().stream()
        .map(entry -> toCurrentView(entry.getKey(), entry.getValue()))
        .flatMap(Optional::stream)
        .sorted(java.util.Comparator.comparingInt(view -> order.getOrDefault(view.batchId(), Integer.MAX_VALUE)))
        .toList();
  }

  private @NonNull Map<UUID, List<RecordRow>> loadRecordsBySetIds(
      @NonNull List<@NonNull UUID> measurementSetIds
  ) {
    final var rows = jdbcTemplate.query(
        """
            SELECT s.measurement_set_id,
                   s.subject_id AS batch_id,
                   revision.name,
                   revision.note,
                   s.version,
                   record.*
              FROM measurement_sets s
              JOIN measurement_set_revisions revision
                ON revision.revision_id = s.current_revision_id
              JOIN measurement_records record
                ON record.revision_id = revision.revision_id
               AND record.role = 'RESULT'
             WHERE s.measurement_set_id IN (:measurementSetIds)
               AND record.characteristic IN (
                   'SPECIFIC_GRAVITY',
                   'PH',
                   'TITRATABLE_ACIDITY',
                   'TANNIN',
                   'YAN'
               )
             ORDER BY s.measurement_set_id, record.characteristic
            """,
        new MapSqlParameterSource("measurementSetIds", measurementSetIds),
        JdbcFeedstockMeasurementQueryRepository::mapRecord
    );
    final Map<UUID, List<RecordRow>> result = new LinkedHashMap<>();
    rows.forEach(row -> result.computeIfAbsent(row.measurementSetId(), _ -> new ArrayList<>()).add(row));
    return result;
  }

  private static @NonNull Optional<@NonNull FeedstockMeasurementView> toView(
      @NonNull SetCandidate candidate,
      @NonNull List<RecordRow> records
  ) {
    final var profile = toChemicalProfile(records);
    return profile.map(value -> new FeedstockMeasurementView(
        FeedstockMeasurementId.of(candidate.measurementSetId()),
        candidate.batchId(),
        FeedstockMeasurementName.of(candidate.name()),
        value,
        candidate.measuredAt(),
        candidate.version(),
        toEvidence(records),
        candidate.note()
    ));
  }

  private static @NonNull Optional<@NonNull FeedstockMeasurementView> toCurrentView(
      @NonNull FeedstockBatchId batchId,
      @NonNull List<RecordRow> records
  ) {
    final var anchor = records.stream()
        .max(java.util.Comparator
            .comparing(RecordRow::measuredAt)
            .thenComparing(RecordRow::recordId)
            .thenComparing(RecordRow::measurementSetId))
        .orElseThrow();
    return toChemicalProfile(records).map(profile -> new FeedstockMeasurementView(
        FeedstockMeasurementId.of(anchor.measurementSetId()),
        batchId,
        FeedstockMeasurementName.of(anchor.name()),
        profile,
        anchor.measuredAt(),
        anchor.version(),
        toEvidence(records),
        anchor.note()
    ));
  }

  private static @NonNull Optional<@NonNull ChemicalProfile> toChemicalProfile(
      @NonNull List<RecordRow> records
  ) {
    final var byCharacteristic =
        new EnumMap<MeasurementCharacteristic, CharacteristicValue<MeasuredValue>>(
            MeasurementCharacteristic.class
        );
    records.stream()
        .filter(recordRow -> CHEMICAL_CHARACTERISTICS.contains(recordRow.characteristic()))
        .forEach(recordRow -> byCharacteristic.put(recordRow.characteristic(), recordRow.value()));
    return MeasurementChemicalProfileProjector.project(byCharacteristic);
  }

  private static @NonNull List<@NonNull FeedstockProfileAxisEvidence> toEvidence(
      @NonNull List<RecordRow> records
  ) {
    return records.stream()
        .map(recordRow -> toAxis(recordRow.characteristic())
            .map(axis -> new FeedstockProfileAxisEvidence(
                axis,
                recordRow.measurementSetId(),
                recordRow.recordId(),
                recordRow.measuredAt(),
                recordRow.source(),
                recordRow.confidence()
            )))
        .flatMap(Optional::stream)
        .toList();
  }

  private static @NonNull Optional<@NonNull FeedstockProfileCharacteristic> toAxis(
      @NonNull MeasurementCharacteristic characteristic
  ) {
    return switch (characteristic) {
      case SPECIFIC_GRAVITY -> Optional.of(FeedstockProfileCharacteristic.SUGAR);
      case PH -> Optional.of(FeedstockProfileCharacteristic.PH);
      case TITRATABLE_ACIDITY -> Optional.of(FeedstockProfileCharacteristic.TITRATABLE_ACIDITY);
      case TANNIN -> Optional.of(FeedstockProfileCharacteristic.TANNIN);
      case YAN -> Optional.of(FeedstockProfileCharacteristic.YAN);
      case TEMPERATURE, TURBIDITY -> Optional.empty();
    };
  }

  private static @NonNull SetCandidate mapCandidate(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    return new SetCandidate(
        resultSet.getObject("measurement_set_id", UUID.class),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
        resultSet.getString("name"),
        resultSet.getString("note"),
        resultSet.getLong("version"),
        resultSet.getTimestamp("measured_at").toInstant()
    );
  }

  private static @NonNull RecordRow mapRecord(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var characteristic = MeasurementCharacteristic.valueOf(resultSet.getString("characteristic"));
    return new RecordRow(
        resultSet.getObject("measurement_set_id", UUID.class),
        FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
        resultSet.getObject("record_id", UUID.class),
        resultSet.getString("name"),
        resultSet.getString("note"),
        resultSet.getLong("version"),
        characteristic,
        MeasurementJdbcReadSupport.value(resultSet, characteristic),
        resultSet.getTimestamp("measured_at").toInstant(),
        resultSet.getString("source_name"),
        nullableConfidence(resultSet.getString("confidence"))
    );
  }

  private static @Nullable ConfidenceLevel nullableConfidence(@Nullable String value) {
    return value == null ? null : ConfidenceLevel.valueOf(value);
  }

  private static @NonNull WorkspaceId workspaceId(
      @NonNull List<@NonNull SetCandidate> candidates,
      @NonNull FeedstockMeasurementId measurementId
  ) {
    return candidates.stream()
        .filter(candidate -> candidate.measurementSetId().equals(measurementId.value()))
        .map(SetCandidate::workspaceId)
        .findFirst()
        .orElseThrow();
  }

  private enum CursorDirection {
    ASCENDING(">", "ASC"),
    DESCENDING("<", "DESC");

    private final @NonNull String operator;
    private final @NonNull String order;

    CursorDirection(
        @NonNull String operator,
        @NonNull String order
    ) {
      this.operator = operator;
      this.order = order;
    }

    private @NonNull String operator() {
      return operator;
    }

    private @NonNull String order() {
      return order;
    }
  }

  private record SetCandidate(
      @NonNull UUID measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockBatchId batchId,
      @NonNull String name,
      @Nullable String note,
      long version,
      @NonNull Instant measuredAt
  ) {
  }

  private record RecordRow(
      @NonNull UUID measurementSetId,
      @NonNull FeedstockBatchId batchId,
      @NonNull UUID recordId,
      @NonNull String name,
      @Nullable String note,
      long version,
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull Instant measuredAt,
      @Nullable String source,
      @Nullable ConfidenceLevel confidence
  ) {
  }
}
