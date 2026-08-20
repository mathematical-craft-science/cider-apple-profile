package org.craft.mcs.cap.core.measurement.application.port.in;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementSetMetadataView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Supplies current measurement-set metadata in one workspace-scoped batch query. */
public interface QueryMeasurementSetMetadataUseCase {

  @NonNull Map<@NonNull MeasurementSetId, @NonNull MeasurementSetMetadataView> findAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSetId> measurementSetIds
  );
}
