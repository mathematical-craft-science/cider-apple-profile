package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMeasurementResultQuery;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Set-based JDBC query for active current measurement RESULT records. */
@Repository
@RequiredArgsConstructor
class JdbcCurrentMeasurementResultQuery implements CurrentMeasurementResultQuery {

  private static final String FIND_BY_SUBJECTS = """
      SELECT s.subject_type,
             s.subject_id,
             s.measurement_set_id,
             r.record_id,
             r.characteristic,
             r.value_kind,
             r.measured_at,
             r.source_type,
             r.source_name,
             r.measurement_method,
             r.source_reference,
             r.confidence,
             r.point_value,
             r.lower_value,
             r.upper_value
        FROM measurement_sets s
        JOIN measurement_records r
          ON r.revision_id = s.current_revision_id
       WHERE s.workspace_id = :workspaceId
         AND s.lifecycle = 'ACTIVE'
         AND s.subject_type = :subjectType
         AND s.subject_id IN (:subjectIds)
         AND r.role = 'RESULT'
       ORDER BY s.subject_id, r.characteristic, r.measured_at DESC, r.record_id DESC
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull List<@NonNull CurrentMeasurementResultView> findBySubjects(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(subjects, "measurement subjects cannot be null");
    if (subjects.isEmpty()) {
      return List.of();
    }

    final var results = new ArrayList<CurrentMeasurementResultView>();
    for (final var subjectType : MeasurementSubjectType.values()) {
      final var subjectIds = subjects.stream()
          .filter(subject -> subject.type() == subjectType)
          .map(MeasurementSubjectReference::id)
          .toList();
      if (!subjectIds.isEmpty()) {
        results.addAll(findBySubjectType(workspaceId, subjectType, subjectIds));
      }
    }
    return List.copyOf(results);
  }

  private @NonNull List<@NonNull CurrentMeasurementResultView> findBySubjectType(
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectType subjectType,
      @NonNull List<@NonNull UUID> subjectIds
  ) {
    return jdbcTemplate.query(
        FIND_BY_SUBJECTS,
        new MapSqlParameterSource()
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectType", subjectType.name())
            .addValue("subjectIds", subjectIds),
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
        MeasurementSetId.of(resultSet.getObject("measurement_set_id", UUID.class)),
        MeasurementRecordId.of(resultSet.getObject("record_id", UUID.class)),
        characteristic,
        MeasurementJdbcReadSupport.value(resultSet, characteristic),
        MeasurementJdbcReadSupport.instant(resultSet, "measured_at"),
        MeasurementJdbcReadSupport.provenance(resultSet)
    );
  }
}
