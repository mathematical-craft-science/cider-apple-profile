package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel.entity.VesselJdbcEntity;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Spring Data persistence primitive for Vessel aggregate rows and revision-checked writes. */
interface SpringDataVesselJdbcRepository extends ListCrudRepository<VesselJdbcEntity, @NonNull UUID> {

  @Query("""
      SELECT vessel_id,
             workspace_id,
             name,
             nominal_capacity_liters,
             material,
             operational_status,
             version,
             created_at,
             updated_at
        FROM vessels
       WHERE vessel_id = :vesselId
         FOR UPDATE
      """)
  @NonNull Optional<@NonNull VesselJdbcEntity> lockById(@Param("vesselId") @NonNull UUID vesselId);

  @Modifying
  @Query("""
      UPDATE vessels
         SET name = :name,
             nominal_capacity_liters = :nominalCapacityLiters,
             material = :material,
             operational_status = :operationalStatus,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE vessel_id = :vesselId
         AND workspace_id = :workspaceId
         AND version = :expectedVersion
      """)
  int updateByIdAndVersion(
      @Param("vesselId") @NonNull UUID vesselId,
      @Param("workspaceId") @NonNull UUID workspaceId,
      @Param("name") @NonNull String name,
      @Param("nominalCapacityLiters") @NonNull BigDecimal nominalCapacityLiters,
      @Param("material") @NonNull VesselMaterial material,
      @Param("operationalStatus") @NonNull VesselOperationalStatus operationalStatus,
      @Param("expectedVersion") long expectedVersion
  );

  boolean existsByWorkspaceIdAndNameIgnoreCaseAndVesselIdNot(
      @NonNull UUID workspaceId,
      @NonNull String name,
      @NonNull UUID vesselId
  );

}
