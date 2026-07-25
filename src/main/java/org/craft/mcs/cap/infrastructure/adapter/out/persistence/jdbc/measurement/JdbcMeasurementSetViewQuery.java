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
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Set-based JDBC read adapter for current logical measurement-set revisions. */
@Repository
@RequiredArgsConstructor
class JdbcMeasurementSetViewQuery implements MeasurementSetViewQuery {

  private static final String SELECT_SETS = """
      SELECT s.measurement_set_id,
             s.workspace_id,
             s.subject_type,
             s.subject_id,
             s.lifecycle,
             s.version,
             s.created_at,
             s.updated_at,
             revision.revision_id,
             revision.revision_number,
             revision.material_state_id,
             revision.name,
             revision.note,
             revision.correction_reason,
             revision.created_at AS revision_created_at,
             revision.actor_reference
        FROM measurement_sets s
        JOIN measurement_set_revisions revision
          ON revision.revision_id = s.current_revision_id
      """;

  private static final String SELECT_RECORDS = """
      SELECT record.revision_id,
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
        FROM measurement_records record
       WHERE record.revision_id IN (:revisionIds)
       ORDER BY record.revision_id,
                record.characteristic,
                record.role,
                record.measured_at,
                record.record_id
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
               ORDER BY s.updated_at DESC, s.measurement_set_id DESC
              """,
        new MapSqlParameterSource()
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectType", subject.type().name())
            .addValue("subjectId", subject.id())
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

    final var recordsByRevision = loadRecords(sets.stream().map(SetRow::revisionId).toList());
    return sets.stream()
        .map(set -> set.toView(recordsByRevision.getOrDefault(set.revisionId(), List.of())))
        .toList();
  }

  private @NonNull Map<MeasurementSetRevisionId, List<MeasurementRecordView>> loadRecords(
      @NonNull List<@NonNull MeasurementSetRevisionId> revisionIds
  ) {
    final var inputs = loadDerivationInputs(revisionIds);
    final var rows = jdbcTemplate.query(
        SELECT_RECORDS,
        new MapSqlParameterSource(
            "revisionIds",
            revisionIds.stream().map(MeasurementSetRevisionId::value).toList()
        ),
        (resultSet, rowNumber) -> mapRecord(resultSet, inputs)
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
            SELECT derived_record_id, input_record_id
              FROM measurement_derivation_inputs
             WHERE revision_id IN (:revisionIds)
             ORDER BY derived_record_id, input_record_id
            """,
        new MapSqlParameterSource(
            "revisionIds",
            revisionIds.stream().map(MeasurementSetRevisionId::value).toList()
        ),
        (resultSet, rowNumber) -> Map.entry(
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

  private static @NonNull SetRow mapSetRow(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    return new SetRow(
        MeasurementSetId.of(resultSet.getObject("measurement_set_id", UUID.class)),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        new MeasurementSubjectReference(
            MeasurementSubjectType.valueOf(resultSet.getString("subject_type")),
            resultSet.getObject("subject_id", UUID.class)
        ),
        MeasurementSetLifecycle.valueOf(resultSet.getString("lifecycle")),
        resultSet.getLong("version"),
        MeasurementSetRevisionId.of(resultSet.getObject("revision_id", UUID.class)),
        MeasurementSetRevisionNumber.of(resultSet.getInt("revision_number")),
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        MeasurementSetName.of(resultSet.getString("name")),
        resultSet.getString("note"),
        correctionReason(resultSet.getString("correction_reason")),
        MeasurementJdbcReadSupport.instant(resultSet, "revision_created_at"),
        ActorReferenceJdbcCodec.decode(resultSet.getString("actor_reference")),
        MeasurementJdbcReadSupport.instant(resultSet, "created_at"),
        MeasurementJdbcReadSupport.instant(resultSet, "updated_at")
    );
  }

  private static @NonNull RecordRow mapRecord(
      @NonNull ResultSet resultSet,
      @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> inputs
  ) throws SQLException {
    final var recordId = MeasurementRecordId.of(resultSet.getObject("record_id", UUID.class));
    final var characteristic = MeasurementCharacteristic.valueOf(resultSet.getString("characteristic"));
    return new RecordRow(
        MeasurementSetRevisionId.of(resultSet.getObject("revision_id", UUID.class)),
        new MeasurementRecordView(
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
        )
    );
  }

  private record SetRow(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetLifecycle lifecycle,
      long version,
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull MeasurementSetRevisionNumber revisionNumber,
      @NonNull MaterialStateId materialStateId,
      @NonNull MeasurementSetName name,
      @Nullable String note,
      @Nullable MeasurementCorrectionReason correctionReason,
      @NonNull Instant revisionCreatedAt,
      @NonNull ActorReference actor,
      @NonNull Instant createdAt,
      @NonNull Instant updatedAt
  ) {

    private @NonNull MeasurementSetView toView(
        @NonNull List<@NonNull MeasurementRecordView> records
    ) {
      return new MeasurementSetView(
          measurementSetId,
          workspaceId,
          subject,
          lifecycle,
          version,
          revisionId,
          revisionNumber,
          materialStateId,
          name,
          note,
          correctionReason,
          revisionCreatedAt,
          actor,
          records,
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
}
