package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.traceability.application.port.in.GetMaterialStateUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementMaterialStateProvider;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/**
 * Reads current state identities from the batch aggregates that own material concurrency.
 *
 * <p>Historical-state validation remains a traceability query because immutable lineage is the
 * authoritative evidence that a state belonged to a subject.</p>
 */
@Repository
@RequiredArgsConstructor
class JdbcMeasurementMaterialStateProvider implements MeasurementMaterialStateProvider {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;
  private final @NonNull GetMaterialStateUseCase materialStateHistory;

  @Override
  public @NonNull MaterialStateId currentStateOf(@NonNull MeasurementSubjectReference subject) {
    requireNonNull(subject, "measurement subject cannot be null");
    return requireNonNull(
        currentStates(subject.type(), null, Set.of(subject)).get(subject),
        "current material state id cannot be null"
    );
  }

  @Override
  public @NonNull MaterialStateId requireObservedState(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId observedStateId
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    requireNonNull(observedStateId, "observed material state id cannot be null");
    final var observedSubject = materialStateHistory.get(observedStateId).state().subject();
    final var expectedSubject = toTraceSubject(subject);
    if (observedSubject.type() != expectedSubject.type()
        || !observedSubject.id().equals(expectedSubject.id())) {
      throw new ApplicationValidationException(
          "observed material state does not belong to the measurement subject"
      );
    }
    return observedStateId;
  }

  @Override
  public @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(subjects, "measurement subjects cannot be null");
    final Map<MeasurementSubjectReference, MaterialStateId> states = new LinkedHashMap<>();
    for (final var subjectType : MeasurementSubjectType.values()) {
      states.putAll(currentStates(subjectType, workspaceId, subjects));
    }
    if (states.size() != subjects.size()) {
      throw new ApplicationValidationException(
          "every measurement subject must resolve to one current material state"
      );
    }
    return Map.copyOf(states);
  }

  private @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStates(
      @NonNull MeasurementSubjectType subjectType,
      @Nullable WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  ) {
    final var subjectIds = subjects.stream()
        .filter(subject -> subject.type() == subjectType)
        .map(MeasurementSubjectReference::id)
        .toList();
    if (subjectIds.isEmpty()) {
      return Map.of();
    }

    final var parameters = new MapSqlParameterSource("subjectIds", subjectIds);
    final var workspacePredicate = workspaceId == null ? "" : " AND workspace_id = :workspaceId";
    if (workspaceId != null) {
      parameters.addValue("workspaceId", workspaceId.value());
    }
    final var rows = jdbcTemplate.query(
        """
            SELECT batch_id, current_material_state_id
              FROM %s
             WHERE batch_id IN (:subjectIds)%s
            """.formatted(tableName(subjectType), workspacePredicate),
        parameters,
        (resultSet, rowNumber) -> Map.entry(
            new MeasurementSubjectReference(
                subjectType,
                resultSet.getObject("batch_id", UUID.class)
            ),
            MaterialStateId.of(resultSet.getObject("current_material_state_id", UUID.class))
        )
    );
    final Map<MeasurementSubjectReference, MaterialStateId> states = new LinkedHashMap<>();
    rows.forEach(row -> states.put(row.getKey(), row.getValue()));
    if (states.size() != subjectIds.size()) {
      throw new ApplicationValidationException(
          "measurement subject does not have a current material state"
      );
    }
    return Map.copyOf(states);
  }

  private static @NonNull String tableName(@NonNull MeasurementSubjectType subjectType) {
    return switch (subjectType) {
      case FEEDSTOCK_BATCH -> "feedstock_batches";
      case PRODUCTION_BATCH -> "production_batches";
    };
  }

  private static @NonNull BatchTraceSubject toTraceSubject(
      @NonNull MeasurementSubjectReference subject
  ) {
    return BatchTraceSubject.of(
        switch (subject.type()) {
          case FEEDSTOCK_BATCH -> BatchTraceSubjectType.FEEDSTOCK_BATCH;
          case PRODUCTION_BATCH -> BatchTraceSubjectType.PRODUCTION_BATCH;
        },
        subject.id()
    );
  }
}
