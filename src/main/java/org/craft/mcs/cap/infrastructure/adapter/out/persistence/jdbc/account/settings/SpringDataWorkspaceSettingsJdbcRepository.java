package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.settings;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Spring Data command repository for WorkspaceSettings JDBC entities. */
interface SpringDataWorkspaceSettingsJdbcRepository
    extends ListCrudRepository<WorkspaceSettingsJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE workspace_settings
         SET measurement_stale_after_seconds = :measurementStaleAfterSeconds,
             default_acidity_basis = :defaultAcidityBasis,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE workspace_id = :workspaceId
         AND version = :expectedVersion
      """)
  int updateByIdAndVersion(
      @Param("workspaceId") @NonNull UUID workspaceId,
      @Param("measurementStaleAfterSeconds") long measurementStaleAfterSeconds,
      @Param("defaultAcidityBasis") @NonNull TitratableAcidityBasis defaultAcidityBasis,
      @Param("expectedVersion") long expectedVersion
  );
}
