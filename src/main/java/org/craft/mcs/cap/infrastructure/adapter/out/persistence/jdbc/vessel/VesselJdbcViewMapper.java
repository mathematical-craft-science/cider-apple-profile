package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.craft.mcs.cap.core.vessel.application.model.VesselView;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;


/** Maps Vessel catalog query rows to their shared application read model. */
final class VesselJdbcViewMapper {

  private VesselJdbcViewMapper() {
  }

  static @NonNull VesselView toView(@NonNull ResultSet resultSet) throws SQLException {
    return new VesselView(
        VesselId.of(resultSet.getObject("vessel_id", UUID.class)),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        resultSet.getString("name"),
        VolumeValue.ofLiters(resultSet.getBigDecimal("nominal_capacity_liters")),
        VesselMaterial.valueOf(resultSet.getString("material")),
        VesselOperationalStatus.valueOf(resultSet.getString("operational_status")),
        AggregateRevision.of(resultSet.getLong("version"))
    );
  }
}
