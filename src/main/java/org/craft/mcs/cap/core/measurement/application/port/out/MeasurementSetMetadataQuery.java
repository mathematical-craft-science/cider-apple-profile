package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementSetMetadataView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Read-side persistence port for current measurement-set metadata. */
public interface MeasurementSetMetadataQuery {

  @NonNull Map<@NonNull MeasurementSetId, @NonNull MeasurementSetMetadataView> findAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSetId> measurementSetIds
  );
}
