package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementResultRetraction;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionChangeKind;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataRetrievalFailureException;
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
             s.created_at,
             s.version,
             r.revision_id,
             r.revision_number,
             r.material_state_id,
             r.lifecycle,
             r.change_kind,
             r.name,
             r.note,
             r.correction_reason,
             r.retraction_reason,
             r.created_at AS revision_created_at,
             r.performed_by_type,
             r.performed_by_id
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
         AND revision_record.record_status = 'ACTIVE'
       ORDER BY record.characteristic, record.role, record.measured_at, record.record_id
      """;

  private static final String FIND_RESULT_RETRACTIONS = """
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
       WHERE revision_record.revision_id = :revisionId
         AND revision_record.record_status = 'RETRACTED'
       ORDER BY record.characteristic, record.record_id
      """;

  private static final String FIND_OBSERVATIONS = """
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

  @NonNull Optional<@NonNull VersionedAggregate<MeasurementSet>> findVersionedById(
      @NonNull MeasurementSetId measurementSetId
  ) {
    try {
      return Optional.of(requireNonNull(jdbcTemplate.queryForObject(
          FIND_SET,
          new MapSqlParameterSource("measurementSetId", measurementSetId.value()),
          (resultSet, rowNumber) -> mapSet(resultSet)
      ), "measurement set query result cannot be null"));
    } catch (EmptyResultDataAccessException exception) {
      return Optional.empty();
    }
  }

  private @NonNull VersionedAggregate<MeasurementSet> mapSet(@NonNull ResultSet resultSet)
      throws SQLException {
    final var revisionId = MeasurementSetRevisionId.of(resultSet.getObject("revision_id", UUID.class));
    final var derivationInputs = findDerivationInputs(revisionId);
    final var records = findRecords(revisionId, derivationInputs);
    final var resultRetractions = findResultRetractions(revisionId, derivationInputs);
    final var revision = new MeasurementSetRevision(
        revisionId,
        MeasurementSetRevisionNumber.of(resultSet.getInt("revision_number")),
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        MeasurementSetLifecycle.valueOf(resultSet.getString("lifecycle")),
        MeasurementSetRevisionChangeKind.valueOf(resultSet.getString("change_kind")),
        MeasurementSetName.of(resultSet.getString("name")),
        resultSet.getString("note"),
        correctionReason(resultSet.getString("correction_reason")),
        retractionReason(resultSet.getString("retraction_reason")),
        MeasurementJdbcReadSupport.instant(resultSet, "revision_created_at"),
        MeasurementJdbcReadSupport.actorReference(
            resultSet,
            "performed_by_type",
            "performed_by_id"
        ),
        records,
        resultRetractions
    );
    final var measurementSet = MeasurementSet.restore(
        MeasurementSetId.of(resultSet.getObject("measurement_set_id", UUID.class)),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        new MeasurementSubjectReference(
            MeasurementSubjectType.valueOf(resultSet.getString("subject_type")),
            resultSet.getObject("subject_id", UUID.class)
        ),
        revision,
        MeasurementJdbcReadSupport.instant(resultSet, "created_at")
    );
    return new VersionedAggregate<>(
        measurementSet,
        AggregateRevision.of(resultSet.getLong("version"))
    );
  }

  private @NonNull List<@NonNull MeasurementResultRetraction> findResultRetractions(
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> derivationInputs
  ) {
    final var rows = jdbcTemplate.query(
        FIND_RESULT_RETRACTIONS,
        new MapSqlParameterSource("revisionId", revisionId.value()),
        (resultSet, _) -> new RetractionRecordRow(
            MeasurementSetRevisionId.of(resultSet.getObject("source_revision_id", UUID.class)),
            MeasurementSetRevisionNumber.of(resultSet.getInt("source_revision_number")),
            mapRecord(resultSet, derivationInputs),
            MeasurementJdbcReadSupport.actorReference(
                resultSet,
                "retraction_actor_type",
                "retraction_actor_id"
            ),
            MeasurementJdbcReadSupport.instant(resultSet, "retracted_at"),
            MeasurementCorrectionReason.of(resultSet.getString("retraction_reason"))
        )
    );
    final var observations = findObservations(rows.stream()
        .flatMap(row -> row.evidence().derivationInputs().stream())
        .toList());
    return rows.stream()
        .map(row -> new MeasurementResultRetraction(
            row.sourceRevisionId(),
            row.sourceRevisionNumber(),
            row.evidence(),
            observationSnapshots(row.evidence(), observations),
            row.actor(),
            row.retractedAt(),
            row.reason()
        ))
        .toList();
  }

  private @NonNull Map<MeasurementRecordId, MeasurementRecord> findObservations(
      @NonNull List<@NonNull MeasurementRecordId> observationIds
  ) {
    if (observationIds.isEmpty()) {
      return Map.of();
    }
    return jdbcTemplate.query(
            FIND_OBSERVATIONS,
            new MapSqlParameterSource(
                "recordIds",
                observationIds.stream().map(MeasurementRecordId::value).toList()
            ),
            (resultSet, _) -> mapRecord(resultSet, Map.of())
        ).stream()
        .collect(Collectors.toUnmodifiableMap(
            MeasurementRecord::recordId,
            Function.identity()
        ));
  }

  private static @NonNull List<@NonNull MeasurementRecord> observationSnapshots(
      @NonNull MeasurementRecord result,
      @NonNull Map<MeasurementRecordId, MeasurementRecord> observations
  ) {
    return result.derivationInputs().stream()
        .map(observationId -> requireObservation(observationId, observations))
        .toList();
  }

  private static @NonNull MeasurementRecord requireObservation(
      @NonNull MeasurementRecordId observationId,
      @NonNull Map<MeasurementRecordId, MeasurementRecord> observations
  ) {
    final var observation = observations.get(observationId);
    if (observation == null) {
      throw new DataRetrievalFailureException(
          "retracted measurement result references missing observation evidence"
      );
    }
    return observation;
  }

  private @NonNull List<@NonNull MeasurementRecord> findRecords(
      @NonNull MeasurementSetRevisionId revisionId,
      @NonNull Map<MeasurementRecordId, List<MeasurementRecordId>> derivationInputs
  ) {
    return jdbcTemplate.query(
        FIND_RECORDS,
        new MapSqlParameterSource("revisionId", revisionId.value()),
        (resultSet, rowNumber) -> mapRecord(resultSet, derivationInputs)
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

  private static @Nullable MeasurementRetractionReason retractionReason(@Nullable String value) {
    return value == null ? null : MeasurementRetractionReason.of(value);
  }

  private record RetractionRecordRow(
      @NonNull MeasurementSetRevisionId sourceRevisionId,
      @NonNull MeasurementSetRevisionNumber sourceRevisionNumber,
      @NonNull MeasurementRecord evidence,
      @NonNull ActorReference actor,
      @NonNull Instant retractedAt,
      @NonNull MeasurementCorrectionReason reason
  ) {
  }
}
