package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetAlreadyExistsException;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
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
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.methodValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.referenceValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/**
 * JDBC adapter that appends immutable measurement revisions and records without aggregate upsert.
 */
@Repository
@RequiredArgsConstructor
class JdbcMeasurementSetRepository implements MeasurementSetRepository {

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
             r.name,
             r.note,
             r.correction_reason,
             r.created_at AS revision_created_at,
             r.actor_reference
        FROM measurement_sets s
        JOIN measurement_set_revisions r
          ON r.revision_id = s.current_revision_id
       WHERE s.measurement_set_id = :measurementSetId
      """;

  private static final String FIND_RECORDS = """
      SELECT record_id,
             characteristic,
             role,
             value_kind,
             measured_at,
             source_type,
             source_name,
             measurement_method,
             source_reference,
             confidence,
             point_value,
             lower_value,
             upper_value,
             derivation_method
        FROM measurement_records
       WHERE revision_id = :revisionId
       ORDER BY characteristic, role, measured_at, record_id
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull VersionedAggregate<MeasurementSet> create(@NonNull MeasurementSet measurementSet) {
    requireNonNull(measurementSet, "measurement set cannot be null");
    try {
      return insertNewSet(measurementSet);
    } catch (DuplicateKeyException exception) {
      throw new MeasurementSetAlreadyExistsException(measurementSet.measurementSetId(), exception);
    }
  }

  private @NonNull VersionedAggregate<MeasurementSet> insertNewSet(
      @NonNull MeasurementSet measurementSet
  ) {
    final var revision = measurementSet.currentRevision();
    jdbcTemplate.update(
        """
            INSERT INTO measurement_sets (
                measurement_set_id,
                workspace_id,
                subject_type,
                subject_id,
                lifecycle,
                current_revision_id,
                created_at,
                updated_at,
                version
            ) VALUES (
                :measurementSetId,
                :workspaceId,
                :subjectType,
                :subjectId,
                :lifecycle,
                :currentRevisionId,
                :createdAt,
                :updatedAt,
                0
            )
            """,
        setParameters(measurementSet)
    );
    insertRevision(measurementSet.measurementSetId(), revision);
    return new VersionedAggregate<>(measurementSet, AggregateRevision.of(0));
  }

  @Override
  public @NonNull VersionedAggregate<MeasurementSet> appendRevision(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    requireNonNull(state, "versioned measurement set cannot be null");
    final var set = state.aggregate();
    final var affectedRows = jdbcTemplate.update(
        """
            UPDATE measurement_sets
               SET current_revision_id = :currentRevisionId,
                   updated_at = :updatedAt,
                   version = version + 1
             WHERE measurement_set_id = :measurementSetId
               AND version = :expectedVersion
               AND lifecycle = 'ACTIVE'
            """,
        setParameters(set).addValue("expectedVersion", state.revision().value())
    );
    requireOneUpdatedRow(affectedRows);
    insertRevision(set.measurementSetId(), set.currentRevision());
    return new VersionedAggregate<>(set, AggregateRevision.of(state.revision().value() + 1));
  }

  @Override
  public @NonNull VersionedAggregate<MeasurementSet> retract(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    requireNonNull(state, "versioned measurement set cannot be null");
    final var set = state.aggregate();
    final var affectedRows = jdbcTemplate.update(
        """
            UPDATE measurement_sets
               SET lifecycle = :lifecycle,
                   updated_at = :updatedAt,
                   version = version + 1
             WHERE measurement_set_id = :measurementSetId
               AND version = :expectedVersion
               AND lifecycle = 'ACTIVE'
            """,
        setParameters(set).addValue("expectedVersion", state.revision().value())
    );
    requireOneUpdatedRow(affectedRows);
    return new VersionedAggregate<>(set, AggregateRevision.of(state.revision().value() + 1));
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<MeasurementSet>> findVersionedById(
      @NonNull MeasurementSetId measurementSetId
  ) {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    try {
      return Optional.of(requireNonNull(jdbcTemplate.queryForObject(
          FIND_SET,
          new MapSqlParameterSource("measurementSetId", measurementSetId.value()),
          (resultSet, rowNumber) -> mapSet(resultSet)
      )));
    } catch (EmptyResultDataAccessException exception) {
      return Optional.empty();
    }
  }

  private void insertRevision(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementSetRevision revision
  ) {
    jdbcTemplate.update(
        """
            INSERT INTO measurement_set_revisions (
                revision_id,
                measurement_set_id,
                revision_number,
                name,
                note,
                correction_reason,
                created_at,
                actor_reference
            ) VALUES (
                :revisionId,
                :measurementSetId,
                :revisionNumber,
                :name,
                :note,
                :correctionReason,
                :revisionCreatedAt,
                :actorReference
            )
            """,
        new MapSqlParameterSource()
            .addValue("revisionId", revision.revisionId().value())
            .addValue("measurementSetId", measurementSetId.value())
            .addValue("revisionNumber", revision.revisionNumber().value())
            .addValue("name", revision.name().value())
            .addValue("note", revision.note())
            .addValue(
                "correctionReason",
                revision.correctionReason() == null ? null : revision.correctionReason().value()
            )
            .addValue("revisionCreatedAt", timestamp(revision.createdAt()))
            .addValue("actorReference", ActorReferenceJdbcCodec.encode(revision.actor()))
    );
    revision.records().forEach(measurementRecord -> insertRecord(revision.revisionId(), measurementRecord));
    revision.records().stream()
        .filter(measurementRecord -> !measurementRecord.derivationInputs().isEmpty())
        .forEach(measurementRecord -> insertDerivationInputs(revision.revisionId(), measurementRecord));
  }

  private void insertRecord(
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull MeasurementRecord measurementRecord
  ) {
    final var parameters = new MapSqlParameterSource()
        .addValue("recordId", measurementRecord.recordId().value())
        .addValue("revisionId", revisionId.value())
        .addValue("characteristic", measurementRecord.characteristic().name())
        .addValue("role", measurementRecord.role().name())
        .addValue("valueKind", measurementRecord.valueKind().name())
        .addValue("measuredAt", timestamp(measurementRecord.measuredAt()))
        .addValue("sourceType", measurementRecord.provenance().sourceType().name())
        .addValue("sourceName", sourceNameValue(measurementRecord.provenance().sourceName()))
        .addValue("measurementMethod", methodValue(measurementRecord.provenance().method()))
        .addValue("sourceReference", referenceValue(measurementRecord.provenance().reference()))
        .addValue("confidence", enumName(measurementRecord.provenance().confidence()))
        .addValue("derivationMethod", enumName(measurementRecord.derivationMethod()));
    addValueParameters(parameters, measurementRecord.value());
    jdbcTemplate.update(
        """
            INSERT INTO measurement_records (
                record_id,
                revision_id,
                characteristic,
                role,
                value_kind,
                measured_at,
                source_type,
                source_name,
                measurement_method,
                source_reference,
                confidence,
                point_value,
                lower_value,
                upper_value,
                derivation_method
            ) VALUES (
                :recordId,
                :revisionId,
                :characteristic,
                :role,
                :valueKind,
                :measuredAt,
                :sourceType,
                :sourceName,
                :measurementMethod,
                :sourceReference,
                :confidence,
                :pointValue,
                :lowerValue,
                :upperValue,
                :derivationMethod
            )
            """,
        parameters
    );
  }

  private void insertDerivationInputs(
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull MeasurementRecord result
  ) {
    result.derivationInputs().forEach(inputId -> jdbcTemplate.update(
        """
            INSERT INTO measurement_derivation_inputs (
                revision_id,
                characteristic,
                derived_record_id,
                derived_role,
                input_record_id,
                input_role
            ) VALUES (
                :revisionId,
                :characteristic,
                :derivedRecordId,
                'RESULT',
                :inputRecordId,
                'OBSERVATION'
            )
            """,
        new MapSqlParameterSource()
            .addValue("revisionId", revisionId.value())
            .addValue("characteristic", result.characteristic().name())
            .addValue("derivedRecordId", result.recordId().value())
            .addValue("inputRecordId", inputId.value())
    ));
  }

  private @NonNull VersionedAggregate<MeasurementSet> mapSet(@NonNull ResultSet resultSet)
      throws SQLException {
    final var revisionId = MeasurementSetRevisionId.of(resultSet.getObject("revision_id", UUID.class));
    final var records = findRecords(revisionId);
    final var revision = new MeasurementSetRevision(
        revisionId,
        MeasurementSetRevisionNumber.of(resultSet.getInt("revision_number")),
        MeasurementSetName.of(resultSet.getString("name")),
        resultSet.getString("note"),
        correctionReason(resultSet.getString("correction_reason")),
        MeasurementJdbcReadSupport.instant(resultSet, "revision_created_at"),
        ActorReferenceJdbcCodec.decode(resultSet.getString("actor_reference")),
        records
    );
    final var set = MeasurementSet.restore(
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
    return new VersionedAggregate<>(set, AggregateRevision.of(resultSet.getLong("version")));
  }

  private @NonNull List<@NonNull MeasurementRecord> findRecords(
      @NonNull MeasurementSetRevisionId revisionId
  ) {
    final Map<MeasurementRecordId, List<MeasurementRecordId>> inputs = findDerivationInputs(revisionId);
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
             WHERE d.revision_id = :revisionId
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

  private static void addValueParameters(
      @NonNull MapSqlParameterSource parameters,
      @NonNull CharacteristicValue<MeasuredValue> value
  ) {
    switch (value) {
      case PointCharacteristicValue<MeasuredValue>(var measuredValue) -> parameters
          .addValue("pointValue", measuredValue.asBigDecimal())
          .addValue("lowerValue", null)
          .addValue("upperValue", null);
      case IntervalCharacteristicValue<MeasuredValue>(var lower, var upper) -> parameters
          .addValue("pointValue", null)
          .addValue("lowerValue", lower.asBigDecimal())
          .addValue("upperValue", upper.asBigDecimal());
    }
  }

  private static @NonNull MapSqlParameterSource setParameters(@NonNull MeasurementSet set) {
    return new MapSqlParameterSource()
        .addValue("measurementSetId", set.measurementSetId().value())
        .addValue("workspaceId", set.workspaceId().value())
        .addValue("subjectType", set.subject().type().name())
        .addValue("subjectId", set.subject().id())
        .addValue("lifecycle", set.lifecycle().name())
        .addValue("currentRevisionId", set.currentRevision().revisionId().value())
        .addValue("createdAt", timestamp(set.createdAt()))
        .addValue("updatedAt", timestamp(set.updatedAt()));
  }

  private static void requireOneUpdatedRow(int affectedRows) {
    if (affectedRows != 1) {
      throw new ConcurrentResourceModificationException("measurement set", null);
    }
  }

  private static @Nullable String enumName(@Nullable Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static @Nullable MeasurementCorrectionReason correctionReason(@Nullable String value) {
    return value == null ? null : MeasurementCorrectionReason.of(value);
  }

  private static @NonNull OffsetDateTime timestamp(@NonNull Instant value) {
    return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
  }
}
