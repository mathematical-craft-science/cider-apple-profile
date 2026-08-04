package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.Map;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementMaterialStateLineage;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Supplies Measurement with state ancestry without exposing the owning Traceability model. */
public interface MeasurementMaterialStateLineageProvider {

  @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MeasurementMaterialStateLineage> findLineages(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStates
  );
}
