package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMeasurementResultQuery;
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


/** Set-based JDBC query for active current measurement RESULT records. */
@Repository
@RequiredArgsConstructor
class JdbcCurrentMeasurementResultQuery implements CurrentMeasurementResultQuery {

  private static final String FIND_BY_STATES = """
      WITH RECURSIVE requested_states(subject_id, current_state_id) AS (
          VALUES %s
      ),
      state_lineage AS (
          SELECT CAST(:subjectType AS VARCHAR) AS subject_type,
                 requested_state.subject_id,
                 requested_state.current_state_id,
                 requested_state.current_state_id AS material_state_id,
                 0 AS state_depth,
                 ARRAY[requested_state.current_state_id] AS visited_states,
                 ARRAY[]::VARCHAR[] AS invalidated_characteristics
            FROM requested_states requested_state

          UNION ALL

          SELECT lineage.subject_type,
                 lineage.subject_id,
                 lineage.current_state_id,
                 input_state.material_state_id,
                 lineage.state_depth + 1,
                 lineage.visited_states || input_state.material_state_id,
                 lineage.invalidated_characteristics || output_state.invalidated_characteristics
            FROM state_lineage lineage
            JOIN batch_trace_step_subjects output_state
              ON output_state.material_state_id = lineage.material_state_id
             AND output_state.subject_role = 'OUTPUT'
             AND output_state.subject_type = lineage.subject_type
             AND output_state.subject_id = lineage.subject_id
            JOIN batch_trace_step_subjects input_state
              ON input_state.step_id = output_state.step_id
             AND input_state.subject_role = 'INPUT'
             AND input_state.subject_type = lineage.subject_type
             AND input_state.subject_id = lineage.subject_id
           WHERE NOT input_state.material_state_id = ANY(lineage.visited_states)
      ),
      distinct_lineage AS (
          SELECT subject_type,
                 subject_id,
                 current_state_id,
                 material_state_id,
                 MIN(state_depth) AS state_depth,
                 invalidated_characteristics
            FROM state_lineage
           GROUP BY subject_type,
                    subject_id,
                    current_state_id,
                    material_state_id,
                    invalidated_characteristics
      ),
      ranked_results AS (
          SELECT measurement_set.subject_type,
                 measurement_set.subject_id,
                 lineage.current_state_id,
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
                 record.upper_value,
                 ROW_NUMBER() OVER (
                     PARTITION BY measurement_set.subject_type,
                                  measurement_set.subject_id,
                                  record.characteristic
                     ORDER BY lineage.state_depth,
                              record.measured_at DESC,
                              record.record_id DESC
                 ) AS selection_rank
            FROM distinct_lineage lineage
            JOIN measurement_sets measurement_set
              ON measurement_set.workspace_id = :workspaceId
             AND measurement_set.subject_type = lineage.subject_type
             AND measurement_set.subject_id = lineage.subject_id
             AND measurement_set.lifecycle = 'ACTIVE'
            JOIN measurement_set_revisions revision
              ON revision.revision_id = measurement_set.current_revision_id
             AND revision.material_state_id = lineage.material_state_id
            JOIN measurement_revision_records revision_record
              ON revision_record.revision_id = revision.revision_id
            JOIN measurement_records record
              ON record.record_id = revision_record.record_id
             AND record.role = 'RESULT'
             AND NOT record.characteristic = ANY(lineage.invalidated_characteristics)
      )
      SELECT subject_type,
             subject_id,
             current_state_id,
             material_state_id,
             measurement_set_id,
             record_id,
             characteristic,
             value_kind,
             measured_at,
             source_type,
             source_name,
             measurement_method,
             source_reference,
             confidence,
             point_value,
             lower_value,
             upper_value
        FROM ranked_results
       WHERE selection_rank = 1
       ORDER BY subject_id, characteristic
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull List<@NonNull CurrentMeasurementResultView> findByStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStates
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(currentStates, "current measurement states cannot be null");
    if (currentStates.isEmpty()) {
      return List.of();
    }

    final var results = new ArrayList<CurrentMeasurementResultView>();
    for (final var subjectType : MeasurementSubjectType.values()) {
      final var states = currentStates.entrySet().stream()
          .filter(entry -> entry.getKey().type() == subjectType)
          .toList();
      if (!states.isEmpty()) {
        results.addAll(findBySubjectType(workspaceId, subjectType, states));
      }
    }
    return List.copyOf(results);
  }

  private @NonNull List<@NonNull CurrentMeasurementResultView> findBySubjectType(
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectType subjectType,
      @NonNull List<Map.Entry<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId>> states
  ) {
    final var parameters = new MapSqlParameterSource()
        .addValue("workspaceId", workspaceId.value())
        .addValue("subjectType", subjectType.name());
    final var requestedStates = new ArrayList<String>();
    for (var index = 0; index < states.size(); index++) {
      final var state = states.get(index);
      final var subjectParameter = "subjectId" + index;
      final var stateParameter = "stateId" + index;
      requestedStates.add(
          "(CAST(:" + subjectParameter + " AS UUID), CAST(:" + stateParameter + " AS UUID))"
      );
      parameters
          .addValue(subjectParameter, state.getKey().id())
          .addValue(stateParameter, state.getValue().value());
    }
    return jdbcTemplate.query(
        FIND_BY_STATES.formatted(String.join(", ", requestedStates)),
        parameters,
        JdbcCurrentMeasurementResultQuery::mapResult
    );
  }

  private static @NonNull CurrentMeasurementResultView mapResult(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var characteristic = MeasurementCharacteristic.valueOf(resultSet.getString("characteristic"));
    return new CurrentMeasurementResultView(
        new MeasurementSubjectReference(
            MeasurementSubjectType.valueOf(resultSet.getString("subject_type")),
            resultSet.getObject("subject_id", UUID.class)
        ),
        MaterialStateId.of(resultSet.getObject("current_state_id", UUID.class)),
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
