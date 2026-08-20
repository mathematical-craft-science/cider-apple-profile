package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementSetMetadataView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetMetadataQuery;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Set-based JDBC adapter for current measurement-set metadata. */
@Repository
@RequiredArgsConstructor
class JdbcMeasurementSetMetadataQuery implements MeasurementSetMetadataQuery {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull Map<@NonNull MeasurementSetId, @NonNull MeasurementSetMetadataView> findAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSetId> measurementSetIds
  ) {
    requireNonNull(workspaceId, "measurement metadata workspace id cannot be null");
    requireNonNull(measurementSetIds, "measurement set metadata ids cannot be null");
    if (measurementSetIds.isEmpty()) {
      return Map.of();
    }
    return jdbcTemplate.query(
            """
                SELECT measurement_set.measurement_set_id,
                       measurement_set.version,
                       revision.name,
                       revision.note
                  FROM measurement_sets measurement_set
                  JOIN measurement_set_heads head
                    ON head.measurement_set_id = measurement_set.measurement_set_id
                  JOIN measurement_set_revisions revision
                    ON revision.measurement_set_id = head.measurement_set_id
                   AND revision.revision_id = head.revision_id
                 WHERE measurement_set.workspace_id = :workspaceId
                   AND measurement_set.measurement_set_id IN (:measurementSetIds)
                """,
            new MapSqlParameterSource()
                .addValue("workspaceId", workspaceId.value())
                .addValue(
                    "measurementSetIds",
                    measurementSetIds.stream().map(MeasurementSetId::value).toList()
            ),
            (resultSet, rowNumber) -> new MeasurementSetMetadataView(
                MeasurementSetId.of(resultSet.getObject("measurement_set_id", UUID.class)),
                MeasurementSetName.of(resultSet.getString("name")),
                resultSet.getString("note"),
                resultSet.getLong("version")
            )
        )
        .stream()
        .collect(Collectors.toUnmodifiableMap(MeasurementSetMetadataView::measurementSetId, value -> value));
  }
}
