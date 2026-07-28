package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;


/** Hydrates the current immutable revision and its evidence as one measurement aggregate. */
@Component
@RequiredArgsConstructor
final class JdbcMeasurementSetAggregateReader {

  private static final String FIND_SET = """
      SELECT s.measurement_set_id,
             s.workspace_id,
             s.subject_type,
             s.subject_id,
             s.lifecycle,
             s.created_at,
             s.updated_at,
             s.version,
             r.revision_id,
             r.revision_number,
             r.material_state_id,
             r.name,
             r.note,
             r.correction_reason,
             r.created_at AS revision_created_at,
             r.actor_reference
        FROM measurement_sets s
        JOIN measurement_set_heads head
          ON head.measurement_set_id = s.measurement_set_id
        JOIN measurement_set_revisions r
          ON r.measurement_set_id = head.measurement_set_id
         AND r.revision_id = head.revision_id
       WHERE s.measurement_set_id = :measurementSetId
      """;

  private static final String FIND_RECORDS = """
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
        FROM measurement_revision_records revision_record
        JOIN measurement_records record ON record.record_id = revision_record.record_id
       WHERE revision_record.revision_id = :revisionId
       ORDER BY record.characteristic, record.role, record.measured_at, record.record_id
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @NonNull Optional<@NonNull VersionedAggregate<MeasurementSet>> findVersionedById(
      @NonNull MeasurementSetId measurementSetId
  ) {
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(
          FIND_SET,
          new MapSqlParameterSource("measurementSetId", measurementSetId.value()),
          (resultSet, rowNumber) -> mapSet(resultSet)
      ));
    } catch (EmptyResultDataAccessException exception) {
      return Optional.empty();
    }
  }

  private @NonNull VersionedAggregate<MeasurementSet> mapSet(@NonNull ResultSet resultSet)
      throws SQLException {
    final var revisionId = MeasurementSetRevisionId.of(resultSet.getObject("revision_id", UUID.class));
    final var records = findRecords(revisionId);
    final var revision = new MeasurementSetRevision(
        revisionId,
        MeasurementSetRevisionNumber.of(resultSet.getInt("revision_number")),
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        MeasurementSetName.of(resultSet.getString("name")),
        resultSet.getString("note"),
        correctionReason(resultSet.getString("correction_reason")),
        MeasurementJdbcReadSupport.instant(resultSet, "revision_created_at"),
        ActorReferenceJdbcCodec.decode(resultSet.getString("actor_reference")),
        records
    );
    final var measurementSet = MeasurementSet.restore(
        MeasurementSetId.of(resultSet.getObject("measurement_set_id", UUID.class)),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        new MeasurementSubjectReference(
            MeasurementSubjectType.valueOf(resultSet.getString("subject_type")),
            resultSet.getObject("subject_id", UUID.class)
        ),
        MeasurementSetLifecycle.valueOf(resultSet.getString("lifecycle")),
        revision,
        MeasurementJdbcReadSupport.instant(resultSet, "created_at"),
        MeasurementJdbcReadSupport.instant(resultSet, "updated_at")
    );
    return new VersionedAggregate<>(
        measurementSet,
        AggregateRevision.of(resultSet.getLong("version"))
    );
  }

  private @NonNull List<@NonNull MeasurementRecord> findRecords(
      @NonNull MeasurementSetRevisionId revisionId
  ) {
    final Map<MeasurementRecordId, List<MeasurementRecordId>> inputs =
        findDerivationInputs(revisionId);
    return jdbcTemplate.query(
        FIND_RECORDS,
        new MapSqlParameterSource("revisionId", revisionId.value()),
        (resultSet, rowNumber) -> mapRecord(resultSet, inputs)
    );
  }

  private @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> findDerivationInputs(
      @NonNull MeasurementSetRevisionId revisionId
  ) {
    final Map<MeasurementRecordId, List<MeasurementRecordId>> result = new HashMap<>();
    final var pairs = jdbcTemplate.query(
        """
            SELECT d.derived_record_id, d.input_record_id
              FROM measurement_derivation_inputs d
              JOIN measurement_revision_records revision_record
                ON revision_record.record_id = d.derived_record_id
             WHERE revision_record.revision_id = :revisionId
             ORDER BY d.derived_record_id, d.input_record_id
        """,
        new MapSqlParameterSource("revisionId", revisionId.value()),
        (resultSet, rowNumber) -> Map.entry(
            MeasurementRecordId.of(resultSet.getObject("derived_record_id", UUID.class)),
            MeasurementRecordId.of(resultSet.getObject("input_record_id", UUID.class))
        )
    );
    pairs.forEach(pair -> result.computeIfAbsent(pair.getKey(), _ -> new ArrayList<>())
        .add(pair.getValue()));
    return result;
  }

  private static @NonNull MeasurementRecord mapRecord(
      @NonNull ResultSet resultSet,
      @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> inputs
  ) throws SQLException {
    final var recordId = MeasurementRecordId.of(resultSet.getObject("record_id", UUID.class));
    final var characteristic = MeasurementCharacteristic.valueOf(resultSet.getString("characteristic"));
    return new MeasurementRecord(
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

  private static @Nullable MeasurementCorrectionReason correctionReason(@Nullable String value) {
    return value == null ? null : MeasurementCorrectionReason.of(value);
  }
}
