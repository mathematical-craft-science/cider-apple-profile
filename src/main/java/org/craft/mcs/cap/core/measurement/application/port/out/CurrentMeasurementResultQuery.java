package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.List;
import java.util.Map;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Reads current active RESULT records without loading measurement aggregates. */
public interface CurrentMeasurementResultQuery {

  @NonNull List<@NonNull CurrentMeasurementResultView> findByStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStates
  );
}
