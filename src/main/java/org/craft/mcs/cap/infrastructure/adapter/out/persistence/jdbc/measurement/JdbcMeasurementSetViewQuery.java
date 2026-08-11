package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementResultRetractionView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetRetractionView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionChangeKind;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Set-based JDBC read adapter for current logical measurement-set revisions. */
@Repository
@RequiredArgsConstructor
class JdbcMeasurementSetViewQuery implements MeasurementSetViewQuery {

  private static final String SELECT_SET_REVISION_COLUMNS = """
      SELECT s.measurement_set_id,
             s.workspace_id,
             s.subject_type,
             s.subject_id,
             s.version,
             s.created_at,
             revision.revision_id,
             revision.revision_number,
             revision.material_state_id,
             revision.lifecycle,
             revision.change_kind,
             revision.name,
             revision.note,
             revision.correction_reason,
             revision.retraction_reason,
             revision.created_at AS revision_created_at,
             revision.performed_by_type,
             revision.performed_by_id
      """;

  private static final String SELECT_SETS = SELECT_SET_REVISION_COLUMNS + """
        FROM measurement_sets s
        JOIN measurement_set_heads head
          ON head.measurement_set_id = s.measurement_set_id
        JOIN measurement_set_revisions revision
          ON revision.measurement_set_id = head.measurement_set_id
         AND revision.revision_id = head.revision_id
      """;

  private static final String SELECT_SET_REVISIONS = SELECT_SET_REVISION_COLUMNS + """
        FROM measurement_sets s
        JOIN measurement_set_revisions revision
          ON revision.measurement_set_id = s.measurement_set_id
      """;

  private static final String SELECT_RECORDS = """
      SELECT revision_record.revision_id,
             record.record_id,
             record.characteristic,
             record.role,
             record.value_kind,
             record.measured_at,
             record.source_type,
             record.source_name,
             record.measurement_method,
             record.source_reference,
             record.confidence,
             record.point_value,
             record.lower_value,
             record.upper_value,
             record.derivation_method
       FROM measurement_revision_records revision_record
        JOIN measurement_records record ON record.record_id = revision_record.record_id
       WHERE revision_record.revision_id IN (:revisionIds)
         AND revision_record.record_status = 'ACTIVE'
       ORDER BY revision_record.revision_id,
                record.characteristic,
                record.role,
                record.measured_at,
                record.record_id
      """;

  private static final String SELECT_RESULT_RETRACTIONS = """
      SELECT revision_record.revision_id,
             record.record_id,
             record.characteristic,
             record.role,
             record.value_kind,
             record.measured_at,
             record.source_type,
             record.source_name,
             record.measurement_method,
             record.source_reference,
             record.confidence,
             record.point_value,
             record.lower_value,
             record.upper_value,
             record.derivation_method,
             retraction_revision.performed_by_type AS retraction_actor_type,
             retraction_revision.performed_by_id AS retraction_actor_id,
             retraction_revision.created_at AS retracted_at,
             retraction_revision.correction_reason AS retraction_reason,
             source_revision.revision_id AS source_revision_id,
             source_revision.revision_number AS source_revision_number
        FROM measurement_revision_records revision_record
        JOIN measurement_records record ON record.record_id = revision_record.record_id
        JOIN measurement_set_revisions current_revision
          ON current_revision.revision_id = revision_record.revision_id
        JOIN LATERAL (
            SELECT candidate.revision_number,
                   candidate.performed_by_type,
                   candidate.performed_by_id,
                   candidate.created_at,
                   candidate.correction_reason
              FROM measurement_revision_records retraction_link
              JOIN measurement_set_revisions candidate
                ON candidate.revision_id = retraction_link.revision_id
             WHERE retraction_link.record_id = revision_record.record_id
               AND retraction_link.record_status = 'RETRACTED'
               AND candidate.measurement_set_id = current_revision.measurement_set_id
               AND candidate.revision_number <= current_revision.revision_number
               AND candidate.change_kind = 'CORRECTION'
             ORDER BY candidate.revision_number
             LIMIT 1
        ) retraction_revision ON TRUE
        JOIN LATERAL (
            SELECT candidate.revision_id,
                   candidate.revision_number
              FROM measurement_revision_records source_link
              JOIN measurement_set_revisions candidate
                ON candidate.revision_id = source_link.revision_id
             WHERE source_link.record_id = revision_record.record_id
               AND source_link.record_status = 'ACTIVE'
               AND candidate.measurement_set_id = current_revision.measurement_set_id
               AND candidate.revision_number < retraction_revision.revision_number
             ORDER BY candidate.revision_number DESC
             LIMIT 1
        ) source_revision ON TRUE
       WHERE revision_record.revision_id IN (:revisionIds)
         AND revision_record.record_status = 'RETRACTED'
       ORDER BY revision_record.revision_id,
                record.characteristic,
                record.record_id
      """;

  private static final String SELECT_OBSERVATIONS = """
      SELECT record.record_id,
             record.characteristic,
             record.role,
             record.value_kind,
             record.measured_at,
             record.source_type,
             record.source_name,
             record.measurement_method,
             record.source_reference,
             record.confidence,
             record.point_value,
             record.lower_value,
             record.upper_value,
             record.derivation_method
        FROM measurement_records record
       WHERE record.record_id IN (:recordIds)
         AND record.role = 'OBSERVATION'
       ORDER BY record.measured_at, record.record_id
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull Optional<@NonNull MeasurementSetView> findById(
      @NonNull MeasurementSetId measurementSetId
  ) {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    return loadViews(
        SELECT_SETS + " WHERE s.measurement_set_id = :measurementSetId",
        new MapSqlParameterSource("measurementSetId", measurementSetId.value())
    ).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull MeasurementSetView> findBySubject(
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(subject, "measurement subject cannot be null");
    return loadViews(
        SELECT_SETS
            + """
               WHERE s.workspace_id = :workspaceId
                 AND s.subject_type = :subjectType
                 AND s.subject_id = :subjectId
               ORDER BY revision.created_at DESC, s.measurement_set_id DESC
              """,
        new MapSqlParameterSource()
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectType", subject.type().name())
            .addValue("subjectId", subject.id())
    );
  }

  @Override
  public @NonNull List<@NonNull MeasurementSetView> findRevisionsById(
      @NonNull MeasurementSetId measurementSetId
  ) {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    return loadViews(
        SELECT_SET_REVISIONS
            + """
               WHERE s.measurement_set_id = :measurementSetId
               ORDER BY revision.revision_number DESC
              """,
        new MapSqlParameterSource("measurementSetId", measurementSetId.value())
    );
  }

  private @NonNull List<@NonNull MeasurementSetView> loadViews(
      @NonNull String sql,
      @NonNull MapSqlParameterSource parameters
  ) {
    final var sets = jdbcTemplate.query(sql, parameters, JdbcMeasurementSetViewQuery::mapSetRow);
    if (sets.isEmpty()) {
      return List.of();
    }

    final var revisionIds = sets.stream().map(SetRow::revisionId).toList();
    final var derivationInputs = loadDerivationInputs(revisionIds);
    final var observations = loadObservations(derivationInputs.values().stream()
        .flatMap(List::stream)
        .toList());
    final var recordsByRevision = loadRecords(revisionIds, derivationInputs);
    final var retractionsByRevision = loadResultRetractions(
        revisionIds,
        derivationInputs,
        observations
    );
    return sets.stream()
        .map(set -> set.toView(
            recordsByRevision.getOrDefault(set.revisionId(), List.of()),
            retractionsByRevision.getOrDefault(set.revisionId(), List.of())
        ))
        .toList();
  }

  private @NonNull Map<MeasurementSetRevisionId, List<MeasurementResultRetractionView>>
      loadResultRetractions(
          @NonNull List<@NonNull MeasurementSetRevisionId> revisionIds,
          @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> derivationInputs,
          @NonNull Map<MeasurementRecordId, MeasurementRecordView> observations
      ) {
    final var rows = jdbcTemplate.query(
        SELECT_RESULT_RETRACTIONS,
        revisionIdsParameter(revisionIds),
        (resultSet, _) -> new RetractionRecordViewRow(
            MeasurementSetRevisionId.of(resultSet.getObject("revision_id", UUID.class)),
            MeasurementSetRevisionId.of(resultSet.getObject("source_revision_id", UUID.class)),
            MeasurementSetRevisionNumber.of(resultSet.getInt("source_revision_number")),
            mapRecord(resultSet, derivationInputs).recordView(),
            MeasurementJdbcReadSupport.actorReference(
                resultSet,
                "retraction_actor_type",
                "retraction_actor_id"
            ),
            MeasurementJdbcReadSupport.instant(resultSet, "retracted_at"),
            MeasurementCorrectionReason.of(resultSet.getString("retraction_reason"))
        )
    );
    final Map<MeasurementSetRevisionId, List<MeasurementResultRetractionView>> retractions =
        new LinkedHashMap<>();
    rows.forEach(row -> retractions
        .computeIfAbsent(row.revisionId(), _ -> new ArrayList<>())
        .add(toRetractionView(row, observations)));
    return retractions;
  }

  private static @NonNull MeasurementResultRetractionView toRetractionView(
      @NonNull RetractionRecordViewRow row,
      @NonNull Map<MeasurementRecordId, MeasurementRecordView> observations
  ) {
    return new MeasurementResultRetractionView(
        row.sourceRevisionId(),
        row.sourceRevisionNumber(),
        row.evidence(),
        row.evidence().derivationInputs().stream()
            .map(observationId -> requireObservation(observationId, observations))
            .toList(),
        row.actor(),
        row.retractedAt(),
        row.reason()
    );
  }

  private @NonNull Map<MeasurementRecordId, MeasurementRecordView> loadObservations(
      @NonNull List<@NonNull MeasurementRecordId> observationIds
  ) {
    if (observationIds.isEmpty()) {
      return Map.of();
    }
    return jdbcTemplate.query(
            SELECT_OBSERVATIONS,
            new MapSqlParameterSource(
                "recordIds",
                observationIds.stream().map(MeasurementRecordId::value).toList()
            ),
            (resultSet, _) -> mapRecordView(resultSet, Map.of())
        ).stream()
        .collect(Collectors.toUnmodifiableMap(
            MeasurementRecordView::recordId,
            Function.identity()
        ));
  }

  private static @NonNull MeasurementRecordView requireObservation(
      @NonNull MeasurementRecordId observationId,
      @NonNull Map<MeasurementRecordId, MeasurementRecordView> observations
  ) {
    final var observation = observations.get(observationId);
    if (observation == null) {
      throw new DataRetrievalFailureException(
          "retracted measurement result references missing observation evidence"
      );
    }
    return observation;
  }

  private @NonNull Map<MeasurementSetRevisionId, List<MeasurementRecordView>> loadRecords(
      @NonNull List<@NonNull MeasurementSetRevisionId> revisionIds,
      @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> inputs
  ) {
    final var rows = jdbcTemplate.query(
        SELECT_RECORDS,
        revisionIdsParameter(revisionIds),
        (resultSet, _) -> mapRecord(resultSet, inputs)
    );
    final Map<MeasurementSetRevisionId, List<MeasurementRecordView>> records = new LinkedHashMap<>();
    rows.forEach(row -> records
        .computeIfAbsent(row.revisionId(), _ -> new ArrayList<>())
        .add(row.recordView()));
    return records;
  }

  private @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> loadDerivationInputs(
      @NonNull List<@NonNull MeasurementSetRevisionId> revisionIds
  ) {
    final var pairs = jdbcTemplate.query(
        """
            SELECT DISTINCT derived_record_id, input_record_id
              FROM measurement_derivation_inputs derivation
              JOIN measurement_revision_records revision_record
                ON revision_record.record_id = derivation.derived_record_id
             WHERE revision_record.revision_id IN (:revisionIds)
             ORDER BY derived_record_id, input_record_id
            """,
        revisionIdsParameter(revisionIds),
        (resultSet, _) -> Map.entry(
            MeasurementRecordId.of(resultSet.getObject("derived_record_id", UUID.class)),
            MeasurementRecordId.of(resultSet.getObject("input_record_id", UUID.class))
        )
    );
    final Map<MeasurementRecordId, List<MeasurementRecordId>> inputs = new HashMap<>();
    pairs.forEach(pair -> inputs
        .computeIfAbsent(pair.getKey(), _ -> new ArrayList<>())
        .add(pair.getValue()));
    return inputs;
  }

  private static @NonNull MapSqlParameterSource revisionIdsParameter(
      @NonNull List<@NonNull MeasurementSetRevisionId> revisionIds
  ) {
    return new MapSqlParameterSource(
        "revisionIds",
        revisionIds.stream().map(MeasurementSetRevisionId::value).toList()
    );
  }

  private static @NonNull SetRow mapSetRow(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var lifecycle = MeasurementSetLifecycle.valueOf(resultSet.getString("lifecycle"));
    final var changeKind = MeasurementSetRevisionChangeKind.valueOf(resultSet.getString("change_kind"));
    final var revisionCreatedAt = MeasurementJdbcReadSupport.instant(
        resultSet,
        "revision_created_at"
    );
    final var actor = MeasurementJdbcReadSupport.actorReference(
        resultSet,
        "performed_by_type",
        "performed_by_id"
    );
    final var retractionReason = retractionReason(resultSet.getString("retraction_reason"));
    return new SetRow(
        MeasurementSetId.of(resultSet.getObject("measurement_set_id", UUID.class)),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        new MeasurementSubjectReference(
            MeasurementSubjectType.valueOf(resultSet.getString("subject_type")),
            resultSet.getObject("subject_id", UUID.class)
        ),
        lifecycle,
        setRetraction(changeKind, actor, revisionCreatedAt, retractionReason),
        resultSet.getLong("version"),
        MeasurementSetRevisionId.of(resultSet.getObject("revision_id", UUID.class)),
        MeasurementSetRevisionNumber.of(resultSet.getInt("revision_number")),
        changeKind,
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        MeasurementSetName.of(resultSet.getString("name")),
        resultSet.getString("note"),
        correctionReason(resultSet.getString("correction_reason")),
        retractionReason,
        revisionCreatedAt,
        actor,
        MeasurementJdbcReadSupport.instant(resultSet, "created_at"),
        revisionCreatedAt
    );
  }

  private static @NonNull RecordRow mapRecord(
      @NonNull ResultSet resultSet,
      @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> inputs
  ) throws SQLException {
    return new RecordRow(
        MeasurementSetRevisionId.of(resultSet.getObject("revision_id", UUID.class)),
        mapRecordView(resultSet, inputs)
    );
  }

  private static @NonNull MeasurementRecordView mapRecordView(
      @NonNull ResultSet resultSet,
      @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> inputs
  ) throws SQLException {
    final var recordId = MeasurementRecordId.of(resultSet.getObject("record_id", UUID.class));
    final var characteristic = MeasurementCharacteristic.valueOf(resultSet.getString("characteristic"));
    return new MeasurementRecordView(
        recordId,
        characteristic,
        MeasurementRecordRole.valueOf(resultSet.getString("role")),
        MeasurementJdbcReadSupport.value(resultSet, characteristic),
        MeasurementJdbcReadSupport.instant(resultSet, "measured_at"),
        MeasurementJdbcReadSupport.provenance(resultSet),
        MeasurementJdbcReadSupport.nullableEnum(
            resultSet.getString("derivation_method"),
            MeasurementDerivationMethod.class
        ),
        List.copyOf(inputs.getOrDefault(recordId, List.of()))
    );
  }

  private record SetRow(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetLifecycle lifecycle,
      @Nullable MeasurementSetRetractionView retraction,
      long version,
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull MeasurementSetRevisionNumber revisionNumber,
      @NonNull MeasurementSetRevisionChangeKind changeKind,
      @NonNull MaterialStateId materialStateId,
      @NonNull MeasurementSetName name,
      @Nullable String note,
      @Nullable MeasurementCorrectionReason correctionReason,
      @Nullable MeasurementRetractionReason retractionReason,
      @NonNull Instant revisionCreatedAt,
      @NonNull ActorReference actor,
      @NonNull Instant createdAt,
      @NonNull Instant updatedAt
  ) {

    private @NonNull MeasurementSetView toView(
        @NonNull List<@NonNull MeasurementRecordView> records,
        @NonNull List<@NonNull MeasurementResultRetractionView> resultRetractions
    ) {
      return new MeasurementSetView(
          measurementSetId,
          workspaceId,
          subject,
          lifecycle,
          retraction,
          version,
          revisionId,
          revisionNumber,
          changeKind,
          materialStateId,
          name,
          note,
          correctionReason,
          retractionReason,
          revisionCreatedAt,
          actor,
          records,
          resultRetractions,
          createdAt,
          updatedAt
      );
    }
  }

  private record RecordRow(
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull MeasurementRecordView recordView
  ) {
  }

  private static @Nullable MeasurementCorrectionReason correctionReason(@Nullable String value) {
    return value == null ? null : MeasurementCorrectionReason.of(value);
  }

  private static @Nullable MeasurementRetractionReason retractionReason(@Nullable String value) {
    return value == null ? null : MeasurementRetractionReason.of(value);
  }

  private static @Nullable MeasurementSetRetractionView setRetraction(
      @NonNull MeasurementSetRevisionChangeKind changeKind,
      @NonNull ActorReference actor,
      @NonNull Instant revisionCreatedAt,
      @Nullable MeasurementRetractionReason reason
  ) {
    return changeKind == MeasurementSetRevisionChangeKind.RETRACTION
        ? new MeasurementSetRetractionView(actor, revisionCreatedAt, requireNonNull(reason))
        : null;
  }

  private record RetractionRecordViewRow(
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull MeasurementSetRevisionId sourceRevisionId,
      @NonNull MeasurementSetRevisionNumber sourceRevisionNumber,
      @NonNull MeasurementRecordView evidence,
      @NonNull ActorReference actor,
      @NonNull Instant retractedAt,
      @NonNull MeasurementCorrectionReason reason
  ) {
  }
}
