package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementResultEvidenceView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementResultEvidenceQuery;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Set-based JDBC reader for active Measurement RESULT evidence without lineage policy. */
@Repository
@RequiredArgsConstructor
class JdbcMeasurementResultEvidenceQuery implements MeasurementResultEvidenceQuery {

  private static final String FIND_RESULTS = """
      SELECT measurement_set.subject_id,
             revision.material_state_id,
             measurement_set.measurement_set_id,
             record.record_id,
             record.characteristic,
             record.value_kind,
             record.measured_at,
             record.source_type,
             record.source_name,
             record.measurement_method,
             record.source_reference,
             record.confidence,
             record.point_value,
             record.lower_value,
             record.upper_value
        FROM measurement_sets measurement_set
        JOIN measurement_set_heads head
          ON head.measurement_set_id = measurement_set.measurement_set_id
        JOIN measurement_set_revisions revision
          ON revision.measurement_set_id = head.measurement_set_id
         AND revision.revision_id = head.revision_id
        JOIN measurement_revision_records revision_record
          ON revision_record.revision_id = revision.revision_id
        JOIN measurement_records record
          ON record.record_id = revision_record.record_id
         AND record.role = 'RESULT'
       WHERE measurement_set.workspace_id = :workspaceId
         AND measurement_set.subject_type = :subjectType
         AND measurement_set.subject_id IN (:subjectIds)
         AND revision.lifecycle = 'ACTIVE'
         AND revision.material_state_id IN (:materialStateIds)
         AND revision_record.record_status = 'ACTIVE'
       ORDER BY measurement_set.subject_id,
                revision.material_state_id,
                record.characteristic,
                record.measured_at DESC,
                record.record_id DESC
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull List<@NonNull MeasurementResultEvidenceView> findByMaterialStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull Set<@NonNull MaterialStateId>> states
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(states, "measurement material states cannot be null");
    if (states.isEmpty()) {
      return List.of();
    }

    final var results = new ArrayList<MeasurementResultEvidenceView>();
    for (final var subjectType : MeasurementSubjectType.values()) {
      results.addAll(findBySubjectType(workspaceId, subjectType, states));
    }
    return List.copyOf(results);
  }

  private @NonNull List<@NonNull MeasurementResultEvidenceView> findBySubjectType(
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectType subjectType,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull Set<@NonNull MaterialStateId>> states
  ) {
    final var entries = states.entrySet().stream()
        .filter(entry -> entry.getKey().type() == subjectType)
        .toList();
    if (entries.isEmpty()) {
      return List.of();
    }
    final var subjectIds = entries.stream()
        .map(Map.Entry::getKey)
        .map(MeasurementSubjectReference::id)
        .distinct()
        .toList();
    final var materialStateIds = entries.stream()
        .map(Map.Entry::getValue)
        .flatMap(Set::stream)
        .map(MaterialStateId::value)
        .distinct()
        .toList();
    return jdbcTemplate.query(
        FIND_RESULTS,
        new MapSqlParameterSource()
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectType", subjectType.name())
            .addValue("subjectIds", subjectIds)
            .addValue("materialStateIds", materialStateIds),
        (resultSet, _) -> mapEvidenceResult(resultSet, subjectType)
    );
  }

  private static @NonNull MeasurementResultEvidenceView mapEvidenceResult(
      @NonNull ResultSet resultSet,
      @NonNull MeasurementSubjectType subjectType
  ) throws SQLException {
    final var characteristic = MeasurementCharacteristic.valueOf(resultSet.getString("characteristic"));
    return new MeasurementResultEvidenceView(
        new MeasurementSubjectReference(
            subjectType,
            resultSet.getObject("subject_id", UUID.class)
        ),
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        MeasurementSetId.of(resultSet.getObject("measurement_set_id", UUID.class)),
        MeasurementRecordId.of(resultSet.getObject("record_id", UUID.class)),
        characteristic,
        MeasurementJdbcReadSupport.value(resultSet, characteristic),
        MeasurementJdbcReadSupport.instant(resultSet, "measured_at"),
        MeasurementJdbcReadSupport.provenance(resultSet)
    );
  }
}
