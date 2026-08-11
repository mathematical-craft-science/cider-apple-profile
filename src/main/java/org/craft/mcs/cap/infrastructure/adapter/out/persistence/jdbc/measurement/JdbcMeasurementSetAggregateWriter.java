package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.MeasurementEvidenceIdentityConflictException;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.methodValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.referenceValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/** Persists immutable measurement records and advances the mutable aggregate head with optimistic locking. */
@Component
@RequiredArgsConstructor
final class JdbcMeasurementSetAggregateWriter {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @NonNull VersionedAggregate<MeasurementSet> create(@NonNull MeasurementSet measurementSet) {
    final var revision = measurementSet.currentRevision();
    jdbcTemplate.update(
        """
            INSERT INTO measurement_sets (
                measurement_set_id,
                workspace_id,
                subject_type,
                subject_id,
                created_at,
                version
            ) VALUES (
                :measurementSetId,
                :workspaceId,
                :subjectType,
                :subjectId,
                :createdAt,
                0
            )
            """,
        setParameters(measurementSet)
    );
    insertRevision(measurementSet.measurementSetId(), revision);
    insertHead(measurementSet.measurementSetId(), revision.revisionId());
    return new VersionedAggregate<>(measurementSet, AggregateRevision.of(0));
  }

  @NonNull VersionedAggregate<MeasurementSet> appendRevision(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    final var measurementSet = state.aggregate();
    final var affectedRows = jdbcTemplate.update(
        """
            UPDATE measurement_sets
               SET version = version + 1
             WHERE measurement_set_id = :measurementSetId
               AND version = :expectedVersion
               AND EXISTS (
                   SELECT 1
                     FROM measurement_set_heads current_head
                     JOIN measurement_set_revisions current_revision
                       ON current_revision.measurement_set_id = current_head.measurement_set_id
                      AND current_revision.revision_id = current_head.revision_id
                    WHERE current_head.measurement_set_id = :measurementSetId
                      AND current_revision.lifecycle = 'ACTIVE'
                      AND current_revision.revision_number + 1 = :nextRevisionNumber
                      AND current_revision.material_state_id = :materialStateId
               )
            """,
        setParameters(measurementSet)
            .addValue("expectedVersion", state.revision().value())
            .addValue(
                "nextRevisionNumber",
                measurementSet.currentRevision().revisionNumber().value()
            )
            .addValue(
                "materialStateId",
                measurementSet.currentRevision().materialStateId().value()
            )
    );
    requireOneUpdatedRow(affectedRows);
    insertRevision(measurementSet.measurementSetId(), measurementSet.currentRevision());
    updateHead(
        measurementSet.measurementSetId(),
        measurementSet.currentRevision().revisionId()
    );
    return incremented(state);
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
                material_state_id,
                lifecycle,
                change_kind,
                name,
                note,
                correction_reason,
                retraction_reason,
                created_at,
                performed_by_type,
                performed_by_id
            ) VALUES (
                :revisionId,
                :measurementSetId,
                :revisionNumber,
                :materialStateId,
                :lifecycle,
                :changeKind,
                :name,
                :note,
                :correctionReason,
                :retractionReason,
                :revisionCreatedAt,
                :performedByType,
                :performedById
            )
            """,
        new MapSqlParameterSource()
            .addValue("revisionId", revision.revisionId().value())
            .addValue("measurementSetId", measurementSetId.value())
            .addValue("revisionNumber", revision.revisionNumber().value())
            .addValue("materialStateId", revision.materialStateId().value())
            .addValue("lifecycle", revision.lifecycle().name())
            .addValue("changeKind", revision.changeKind().name())
            .addValue("name", revision.name().value())
            .addValue("note", revision.note())
            .addValue(
                "correctionReason",
                revision.correctionReason() == null ? null : revision.correctionReason().value()
            )
            .addValue(
                "retractionReason",
                revision.retractionReason() == null ? null : revision.retractionReason().value()
            )
            .addValue("revisionCreatedAt", timestamp(revision.createdAt()))
            .addValue("performedByType", revision.actor().type().name())
            .addValue("performedById", revision.actor().id())
    );
    revision.records().forEach(measurementRecord -> {
      insertRecord(measurementSetId, measurementRecord);
      associateActiveRecord(measurementSetId, revision.revisionId(), measurementRecord);
    });
    revision.records().stream()
        .filter(measurementRecord -> !measurementRecord.derivationInputs().isEmpty())
        .forEach(measurementRecord -> insertDerivationInputs(measurementSetId, measurementRecord));
    revision.resultRetractions().forEach(retraction ->
        associateRetractedRecord(measurementSetId, revision.revisionId(), retraction.result())
    );
  }

  private void insertHead(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementSetRevisionId revisionId
  ) {
    jdbcTemplate.update(
        """
            INSERT INTO measurement_set_heads (
                measurement_set_id,
                revision_id
            ) VALUES (
                :measurementSetId,
                :revisionId
            )
            """,
        headParameters(measurementSetId, revisionId)
    );
  }

  private void updateHead(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementSetRevisionId revisionId
  ) {
    final var affectedRows = jdbcTemplate.update(
        """
            UPDATE measurement_set_heads
               SET revision_id = :revisionId
             WHERE measurement_set_id = :measurementSetId
            """,
        headParameters(measurementSetId, revisionId)
    );
    requireOneUpdatedRow(affectedRows);
  }

  private static @NonNull MapSqlParameterSource headParameters(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementSetRevisionId revisionId
  ) {
    return new MapSqlParameterSource()
        .addValue("measurementSetId", measurementSetId.value())
        .addValue("revisionId", revisionId.value());
  }

  private void insertRecord(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementRecord measurementRecord
  ) {
    final var parameters = new MapSqlParameterSource()
        .addValue("measurementSetId", measurementSetId.value())
        .addValue("recordId", measurementRecord.recordId().value())
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
    final var affectedRows = jdbcTemplate.update(
        """
            INSERT INTO measurement_records (
                measurement_set_id,
                record_id,
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
                :measurementSetId,
                :recordId,
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
            ON CONFLICT (record_id) DO UPDATE
                SET record_id = EXCLUDED.record_id
              WHERE measurement_records.measurement_set_id = EXCLUDED.measurement_set_id
                AND measurement_records.characteristic = EXCLUDED.characteristic
                AND measurement_records.role = EXCLUDED.role
                AND measurement_records.value_kind = EXCLUDED.value_kind
                AND measurement_records.measured_at = EXCLUDED.measured_at
                AND measurement_records.source_type = EXCLUDED.source_type
                AND measurement_records.source_name IS NOT DISTINCT FROM EXCLUDED.source_name
                AND measurement_records.measurement_method IS NOT DISTINCT FROM EXCLUDED.measurement_method
                AND measurement_records.source_reference IS NOT DISTINCT FROM EXCLUDED.source_reference
                AND measurement_records.confidence IS NOT DISTINCT FROM EXCLUDED.confidence
                AND measurement_records.point_value IS NOT DISTINCT FROM EXCLUDED.point_value
                AND measurement_records.lower_value IS NOT DISTINCT FROM EXCLUDED.lower_value
                AND measurement_records.upper_value IS NOT DISTINCT FROM EXCLUDED.upper_value
                AND measurement_records.derivation_method IS NOT DISTINCT FROM EXCLUDED.derivation_method
            """,
        parameters
    );
    if (affectedRows != 1) {
      throw new MeasurementEvidenceIdentityConflictException(measurementRecord.recordId());
    }
  }

  private void associateActiveRecord(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull MeasurementRecord measurementRecord
  ) {
    jdbcTemplate.update(
        """
            INSERT INTO measurement_revision_records (
                measurement_set_id,
                revision_id,
                record_id,
                characteristic,
                role,
                record_status
            ) VALUES (
                :measurementSetId,
                :revisionId,
                :recordId,
                :characteristic,
                :role,
                'ACTIVE'
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId.value())
            .addValue("revisionId", revisionId.value())
            .addValue("recordId", measurementRecord.recordId().value())
            .addValue("characteristic", measurementRecord.characteristic().name())
            .addValue("role", measurementRecord.role().name())
    );
  }

  private void associateRetractedRecord(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull MeasurementRecord measurementRecord
  ) {
    jdbcTemplate.update(
        """
            INSERT INTO measurement_revision_records (
                measurement_set_id,
                revision_id,
                record_id,
                characteristic,
                role,
                record_status
            ) VALUES (
                :measurementSetId,
                :revisionId,
                :recordId,
                :characteristic,
                :role,
                'RETRACTED'
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId.value())
            .addValue("revisionId", revisionId.value())
            .addValue("recordId", measurementRecord.recordId().value())
            .addValue("characteristic", measurementRecord.characteristic().name())
            .addValue("role", measurementRecord.role().name())
    );
  }

  private void insertDerivationInputs(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementRecord result
  ) {
    result.derivationInputs().forEach(inputId -> jdbcTemplate.update(
        """
            INSERT INTO measurement_derivation_inputs (
                measurement_set_id,
                characteristic,
                derived_record_id,
                derived_role,
                input_record_id,
                input_role
            ) VALUES (
                :measurementSetId,
                :characteristic,
                :derivedRecordId,
                'RESULT',
                :inputRecordId,
                'OBSERVATION'
            )
            ON CONFLICT (derived_record_id, input_record_id) DO NOTHING
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId.value())
            .addValue("characteristic", result.characteristic().name())
            .addValue("derivedRecordId", result.recordId().value())
            .addValue("inputRecordId", inputId.value())
    ));
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

  private static @NonNull MapSqlParameterSource setParameters(@NonNull MeasurementSet measurementSet) {
    return new MapSqlParameterSource()
        .addValue("measurementSetId", measurementSet.measurementSetId().value())
        .addValue("workspaceId", measurementSet.workspaceId().value())
        .addValue("subjectType", measurementSet.subject().type().name())
        .addValue("subjectId", measurementSet.subject().id())
        .addValue("createdAt", timestamp(measurementSet.createdAt()));
  }

  private static @NonNull VersionedAggregate<MeasurementSet> incremented(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    return new VersionedAggregate<>(
        state.aggregate(),
        AggregateRevision.of(state.revision().value() + 1)
    );
  }

  private static void requireOneUpdatedRow(int affectedRows) {
    if (affectedRows != 1) {
      throw new ConcurrentResourceModificationException("measurement set", null);
    }
  }

  private static @Nullable String enumName(@Nullable Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static @NonNull OffsetDateTime timestamp(@NonNull Instant value) {
    return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
  }
}
