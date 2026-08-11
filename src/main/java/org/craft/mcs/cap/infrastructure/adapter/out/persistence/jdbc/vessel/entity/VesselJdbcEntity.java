package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;


/** Spring Data JDBC row owned by the Vessel aggregate persistence adapter. */
@Table("vessels")
public class VesselJdbcEntity {

  @Id @NonNull @Column("vessel_id") UUID vesselId;
  @NonNull @Column("workspace_id") UUID workspaceId;
  @NonNull @Column("name") String name;
  @NonNull @Column("nominal_capacity_liters") BigDecimal nominalCapacityLiters;
  @NonNull @Column("material") VesselMaterial material;
  @NonNull @Column("operational_status") VesselOperationalStatus operationalStatus;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public VesselJdbcEntity() {
  }

  public VesselJdbcEntity(
      @NonNull UUID vesselId,
      @NonNull UUID workspaceId,
      @NonNull String name,
      @NonNull BigDecimal nominalCapacityLiters,
      @NonNull VesselMaterial material,
      @NonNull VesselOperationalStatus operationalStatus
  ) {
    this.vesselId = requireNonNull(vesselId, "vessel id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.name = requireNonNull(name, "vessel name cannot be null");
    this.nominalCapacityLiters = requireNonNull(
        nominalCapacityLiters,
        "vessel nominal capacity cannot be null"
    );
    this.material = requireNonNull(material, "vessel material cannot be null");
    this.operationalStatus = requireNonNull(operationalStatus, "vessel operational status cannot be null");
  }
}
