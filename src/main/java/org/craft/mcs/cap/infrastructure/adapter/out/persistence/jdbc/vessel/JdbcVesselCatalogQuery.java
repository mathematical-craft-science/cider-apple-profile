package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.model.VesselView;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCatalogQuery;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Dedicated JDBC read adapter for vessel catalog and display-reference queries. */
@Repository
@RequiredArgsConstructor
class JdbcVesselCatalogQuery implements VesselCatalogQuery {

  private static final @NonNull String FIND_BY_ID_SQL = """
      SELECT vessel_id,
             workspace_id,
             name,
             nominal_capacity_liters,
             material,
             operational_status,
             version
        FROM vessels
       WHERE vessel_id = :vesselId
      """;
  private static final @NonNull String FIND_ALL_BY_WORKSPACE_SQL = """
      SELECT vessel_id,
             workspace_id,
             name,
             nominal_capacity_liters,
             material,
             operational_status,
             version
        FROM vessels
       WHERE workspace_id = :workspaceId
       ORDER BY name ASC, vessel_id ASC
      """;
  private static final @NonNull String FIND_REFERENCES_SQL = """
      SELECT vessel_id, name
        FROM vessels
       WHERE workspace_id = :workspaceId
         AND vessel_id IN (:vesselIds)
      """;
  private static final @NonNull RowMapper<@NonNull VesselView> VIEW_ROW_MAPPER =
      (resultSet, _) -> VesselJdbcViewMapper.toView(resultSet);
  private static final @NonNull RowMapper<@NonNull VesselReferenceView> REFERENCE_ROW_MAPPER =
      (resultSet, _) -> toReference(resultSet);

  private final @NonNull NamedParameterJdbcOperations jdbc;

  @Override
  public @NonNull Optional<@NonNull VesselView> findById(@NonNull VesselId vesselId) {
    requireNonNull(vesselId, "vessel id cannot be null");
    return jdbc.query(
            FIND_BY_ID_SQL,
            Map.of("vesselId", vesselId.value()),
            VIEW_ROW_MAPPER
        )
        .stream()
        .findFirst();
  }

  @Override
  public @NonNull List<@NonNull VesselView> findAllByWorkspace(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return jdbc.query(
        FIND_ALL_BY_WORKSPACE_SQL,
        Map.of("workspaceId", workspaceId.value()),
        VIEW_ROW_MAPPER
    );
  }

  @Override
  public @NonNull Map<@NonNull VesselId, @NonNull VesselReferenceView> findReferences(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull VesselId> vesselIds
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(vesselIds, "vessel ids cannot be null");
    if (vesselIds.isEmpty()) {
      return Map.of();
    }
    final var parameters = new MapSqlParameterSource()
        .addValue("workspaceId", workspaceId.value())
        .addValue("vesselIds", vesselIds.stream().map(VesselId::value).toList());
    return jdbc.query(
        FIND_REFERENCES_SQL,
        parameters,
        REFERENCE_ROW_MAPPER
    ).stream().collect(Collectors.toUnmodifiableMap(
        VesselReferenceView::vesselId,
        Function.identity()
    ));
  }

  private static @NonNull VesselReferenceView toReference(@NonNull ResultSet resultSet) throws SQLException {
    return new VesselReferenceView(
        VesselId.of(resultSet.getObject("vessel_id", UUID.class)),
        resultSet.getString("name")
    );
  }
}
