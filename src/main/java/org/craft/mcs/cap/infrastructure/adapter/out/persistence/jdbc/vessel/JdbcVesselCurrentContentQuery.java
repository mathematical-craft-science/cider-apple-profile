package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.VesselUsageProjectionException;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentType;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentView;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** JDBC read adapter combining active Feedstock and Production material assignments by vessel. */
@Repository
@RequiredArgsConstructor
class JdbcVesselCurrentContentQuery implements VesselCurrentContentQuery {

  private static final @NonNull String FIND_BY_VESSEL_ID_SQL = """
      SELECT vessel_id,
             content_type,
             subject_id,
             material_state_id,
             display_name,
             volume_liters
        FROM (
          SELECT vessel_id,
                 'FEEDSTOCK_BATCH' AS content_type,
                 batch_id AS subject_id,
                 current_material_state_id AS material_state_id,
                 name AS display_name,
                 available_volume_liters AS volume_liters
            FROM feedstock_batches
           WHERE lifecycle = 'ACTIVE'
             AND deleted_at IS NULL
          UNION ALL
          SELECT vessel_id,
                 'PRODUCTION_BATCH' AS content_type,
                 batch_id AS subject_id,
                 current_material_state_id AS material_state_id,
                 name AS display_name,
                 current_volume_liters AS volume_liters
            FROM production_batches
           WHERE lifecycle = 'ACTIVE'
        ) current_contents
       WHERE vessel_id = :vesselId
       ORDER BY content_type, subject_id
      """;
  private static final @NonNull String FIND_ALL_BY_WORKSPACE_SQL = """
      SELECT vessel_id,
             content_type,
             subject_id,
             material_state_id,
             display_name,
             volume_liters
        FROM (
          SELECT vessel_id,
                 'FEEDSTOCK_BATCH' AS content_type,
                 batch_id AS subject_id,
                 current_material_state_id AS material_state_id,
                 name AS display_name,
                 available_volume_liters AS volume_liters
            FROM feedstock_batches
           WHERE workspace_id = :workspaceId
             AND lifecycle = 'ACTIVE'
             AND deleted_at IS NULL
          UNION ALL
          SELECT vessel_id,
                 'PRODUCTION_BATCH' AS content_type,
                 batch_id AS subject_id,
                 current_material_state_id AS material_state_id,
                 name AS display_name,
                 current_volume_liters AS volume_liters
            FROM production_batches
           WHERE workspace_id = :workspaceId
             AND lifecycle = 'ACTIVE'
        ) current_contents
       ORDER BY vessel_id, content_type, subject_id
      """;
  private static final @NonNull RowMapper<@NonNull VesselCurrentContentView> CONTENT_ROW_MAPPER =
      (resultSet, _) -> toView(resultSet);

  private final @NonNull NamedParameterJdbcOperations jdbc;

  @Override
  public @NonNull Optional<@NonNull VesselCurrentContentView> findByVesselId(
      @NonNull VesselId vesselId
  ) {
    requireNonNull(vesselId, "vessel id cannot be null");
    final var contents = jdbc.query(
        FIND_BY_VESSEL_ID_SQL,
        Map.of("vesselId", vesselId.value()),
        CONTENT_ROW_MAPPER
    );
    requireSingleContent(vesselId, contents);
    return contents.stream().findFirst();
  }

  @Override
  public @NonNull Map<@NonNull VesselId, @NonNull VesselCurrentContentView> findAllByWorkspace(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    final var contents = jdbc.query(
        FIND_ALL_BY_WORKSPACE_SQL,
        Map.of("workspaceId", workspaceId.value()),
        CONTENT_ROW_MAPPER
    );
    final var byVessel = new LinkedHashMap<VesselId, VesselCurrentContentView>();
    for (final var content : contents) {
      final var previous = byVessel.putIfAbsent(content.vesselId(), content);
      if (previous != null) {
        throw multipleContents(content.vesselId());
      }
    }
    return Map.copyOf(byVessel);
  }

  private static void requireSingleContent(
      @NonNull VesselId vesselId,
      @NonNull List<@NonNull VesselCurrentContentView> contents
  ) {
    if (contents.size() > 1) {
      throw multipleContents(vesselId);
    }
  }

  private static @NonNull VesselUsageProjectionException multipleContents(@NonNull VesselId vesselId) {
    return new VesselUsageProjectionException(
        "vessel %s has multiple active material assignments".formatted(vesselId.value())
    );
  }

  private static @NonNull VesselCurrentContentView toView(@NonNull ResultSet resultSet) throws SQLException {
    return new VesselCurrentContentView(
        VesselId.of(resultSet.getObject("vessel_id", UUID.class)),
        VesselCurrentContentType.valueOf(resultSet.getString("content_type")),
        resultSet.getObject("subject_id", UUID.class),
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        BatchName.of(resultSet.getString("display_name")),
        VolumeValue.ofLiters(resultSet.getBigDecimal("volume_liters"))
    );
  }
}
