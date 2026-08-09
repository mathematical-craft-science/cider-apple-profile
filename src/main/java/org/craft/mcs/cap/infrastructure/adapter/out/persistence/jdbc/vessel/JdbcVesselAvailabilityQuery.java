package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.sql.Types;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.model.VesselView;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselAvailabilityQuery;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** JDBC read adapter selecting operational vessels not occupied by active Feedstock or Production batches. */
@Repository
@RequiredArgsConstructor
class JdbcVesselAvailabilityQuery implements VesselAvailabilityQuery {

  private static final @NonNull String FIND_AVAILABLE_SQL = """
      SELECT vessel_id,
             workspace_id,
             name,
             nominal_capacity_liters,
             material,
             operational_status,
             version
        FROM vessels vessel
       WHERE vessel.workspace_id = :workspaceId
         AND vessel.operational_status = 'OPERATIONAL'
         AND (:nameContains IS NULL OR POSITION(LOWER(:nameContains) IN LOWER(vessel.name)) > 0)
         AND NOT EXISTS (
           SELECT 1
             FROM feedstock_batches feedstock
            WHERE feedstock.workspace_id = vessel.workspace_id
              AND feedstock.vessel_id = vessel.vessel_id
              AND feedstock.lifecycle = 'ACTIVE'
              AND feedstock.deleted_at IS NULL
         )
         AND NOT EXISTS (
           SELECT 1
             FROM production_batches production
            WHERE production.workspace_id = vessel.workspace_id
              AND production.vessel_id = vessel.vessel_id
              AND production.lifecycle = 'ACTIVE'
         )
       ORDER BY LOWER(vessel.name) ASC, vessel.name ASC, vessel.vessel_id ASC
      """;
  private static final @NonNull RowMapper<@NonNull VesselView> VIEW_ROW_MAPPER =
      (resultSet, _) -> VesselJdbcViewMapper.toView(resultSet);

  private final @NonNull NamedParameterJdbcOperations jdbc;

  @Override
  public @NonNull List<@NonNull VesselView> findAvailable(
      @NonNull WorkspaceId workspaceId,
      @Nullable TextSearchCriteria nameContains
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    final var parameters = new MapSqlParameterSource()
        .addValue("workspaceId", workspaceId.value())
        .addValue("nameContains", searchTerm(nameContains), Types.VARCHAR);
    return jdbc.query(
        FIND_AVAILABLE_SQL,
        parameters,
        VIEW_ROW_MAPPER
    );
  }

  private static @Nullable String searchTerm(@Nullable TextSearchCriteria nameContains) {
    return nameContains == null ? null : nameContains.query();
  }
}
