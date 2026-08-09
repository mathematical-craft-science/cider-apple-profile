package org.craft.mcs.cap.testsupport;

import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.JdbcOperations;

import static java.util.UUID.fromString;


/** Stable UUIDv7 vessel identities shared by tests that do not exercise the Vessel catalog itself. */
public final class VesselTestFixtures {

  public static final @NonNull VesselId FEEDSTOCK_VESSEL_ID = VesselId.of(
      fromString("019fd42c-3ac8-7000-91fe-48173dfc0c7e")
  );

  public static final @NonNull VesselId PRODUCTION_VESSEL_ID = VesselId.of(
      fromString("019fd42c-3ac9-7000-91fe-48173dfc0c7e")
  );

  public static final @NonNull VesselId SECONDARY_PRODUCTION_VESSEL_ID = VesselId.of(
      fromString("019fd42c-3aca-7000-91fe-48173dfc0c7e")
  );

  private VesselTestFixtures() {
  }

  public static @NonNull VesselId vesselId(@NonNull String vesselName) {
    return switch (vesselName.strip()) {
      case "Tank A-01" -> FEEDSTOCK_VESSEL_ID;
      case "Tank F-04" -> SECONDARY_PRODUCTION_VESSEL_ID;
      case "Tank F-03", "Tank P-01" -> PRODUCTION_VESSEL_ID;
      default -> VesselId.of(fromString("019fd42c-3acb-7000-91fe-48173dfc0c7e"));
    };
  }

  public static @NonNull VesselReferenceView reference(
      @NonNull VesselId vesselId,
      @NonNull String name
  ) {
    return new VesselReferenceView(vesselId, name);
  }

  public static void persistFeedstockVessel(
      @NonNull JdbcOperations jdbc,
      @NonNull WorkspaceId workspaceId
  ) {
    persist(jdbc, workspaceId, FEEDSTOCK_VESSEL_ID, "Tank A-01");
  }

  public static void persistProductionVessel(
      @NonNull JdbcOperations jdbc,
      @NonNull WorkspaceId workspaceId
  ) {
    persist(jdbc, workspaceId, PRODUCTION_VESSEL_ID, "Tank P-01");
  }

  public static void persistAllVessels(
      @NonNull JdbcOperations jdbc,
      @NonNull WorkspaceId workspaceId
  ) {
    persistFeedstockVessel(jdbc, workspaceId);
    persistProductionVessel(jdbc, workspaceId);
    persist(jdbc, workspaceId, SECONDARY_PRODUCTION_VESSEL_ID, "Tank F-04");
  }

  public static void persist(
      @NonNull JdbcOperations jdbc,
      @NonNull WorkspaceId workspaceId,
      @NonNull VesselId vesselId,
      @NonNull String name
  ) {
    jdbc.update(
        """
            INSERT INTO vessels (
                vessel_id,
                workspace_id,
                name,
                nominal_capacity_liters,
                material,
                operational_status,
                version,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, 1000000, 'STAINLESS_STEEL', 'OPERATIONAL', 0,
                      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        vesselId.value(),
        workspaceId.value(),
        name
    );
  }
}
