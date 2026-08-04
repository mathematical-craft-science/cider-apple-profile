package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.traceability.application.model.MaterialStateLineage;
import org.craft.mcs.cap.core.traceability.application.model.MaterialStateLineageEntry;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateLineageStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Traceability-owned recursive query for immutable material-state ancestry. */
@Repository
@RequiredArgsConstructor
class JdbcMaterialStateLineageStore implements MaterialStateLineageStore {

  private static final String FIND_LINEAGES = """
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
            JOIN batch_trace_step_subjects current_output
              ON current_output.material_state_id = requested_state.current_state_id
             AND current_output.subject_role = 'OUTPUT'
             AND current_output.subject_type = CAST(:subjectType AS VARCHAR)
             AND current_output.subject_id = requested_state.subject_id
            JOIN batch_trace_steps current_step
              ON current_step.step_id = current_output.step_id
             AND current_step.workspace_id = :workspaceId

          UNION ALL

          SELECT lineage.subject_type,
                 lineage.subject_id,
                 lineage.current_state_id,
                 input_state.material_state_id,
                 lineage.state_depth + 1,
                 lineage.visited_states || input_state.material_state_id,
                 lineage.invalidated_characteristics || output_state.invalidated_characteristics
            FROM state_lineage lineage
            JOIN batch_trace_steps step
              ON step.workspace_id = :workspaceId
            JOIN batch_trace_step_subjects output_state
              ON output_state.step_id = step.step_id
             AND output_state.material_state_id = lineage.material_state_id
             AND output_state.subject_role = 'OUTPUT'
             AND output_state.subject_type = lineage.subject_type
             AND output_state.subject_id = lineage.subject_id
            JOIN batch_trace_step_subjects input_state
              ON input_state.step_id = output_state.step_id
             AND input_state.subject_role = 'INPUT'
             AND input_state.subject_type = lineage.subject_type
             AND input_state.subject_id = lineage.subject_id
           WHERE NOT input_state.material_state_id = ANY(lineage.visited_states)
      )
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
       ORDER BY subject_id, state_depth, material_state_id
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateLineage> findLineages(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateId> currentStates
  ) {
    requireNonNull(workspaceId, "lineage workspace id cannot be null");
    requireNonNull(currentStates, "current material states cannot be null");
    final Map<BatchTraceSubject, MaterialStateLineage> lineages = new LinkedHashMap<>();
    for (final var subjectType : BatchTraceSubjectType.values()) {
      lineages.putAll(findBySubjectType(workspaceId, subjectType, currentStates));
    }
    return Map.copyOf(lineages);
  }

  private @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateLineage> findBySubjectType(
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchTraceSubjectType subjectType,
      @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateId> currentStates
  ) {
    final var states = currentStates.entrySet().stream()
        .filter(entry -> entry.getKey().type() == subjectType)
        .toList();
    if (states.isEmpty()) {
      return Map.of();
    }

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

    final var rows = jdbcTemplate.query(
        FIND_LINEAGES.formatted(String.join(", ", requestedStates)),
        parameters,
        JdbcMaterialStateLineageStore::mapEntry
    );
    final Map<BatchTraceSubject, List<MaterialStateLineageEntry>> entriesBySubject = new LinkedHashMap<>();
    rows.forEach(row -> entriesBySubject
        .computeIfAbsent(row.subject(), _ -> new ArrayList<>())
        .add(row.entry()));

    final Map<BatchTraceSubject, MaterialStateLineage> lineages = new LinkedHashMap<>();
    states.forEach(state -> lineages.put(
        state.getKey(),
        new MaterialStateLineage(
            state.getKey(),
            state.getValue(),
            entriesBySubject.getOrDefault(state.getKey(), List.of())
        )
    ));
    return Map.copyOf(lineages);
  }

  private static @NonNull LineageRow mapEntry(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var subject = BatchTraceSubject.of(
        BatchTraceSubjectType.valueOf(resultSet.getString("subject_type")),
        resultSet.getObject("subject_id", UUID.class)
    );
    final var invalidatedValues = (String[]) resultSet.getArray("invalidated_characteristics").getArray();
    return new LineageRow(
        subject,
        new MaterialStateLineageEntry(
            MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
            resultSet.getInt("state_depth"),
            Arrays.stream(invalidatedValues)
                .map(MeasurementCharacteristic::valueOf)
                .collect(Collectors.toUnmodifiableSet())
        )
    );
  }

  private record LineageRow(
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateLineageEntry entry
  ) {
  }
}
