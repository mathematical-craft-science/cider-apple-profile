package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Resolves the current state token required before recording new measurement evidence. */
public interface MeasurementMaterialStateProvider {

  @NonNull MaterialStateId currentStateOf(@NonNull MeasurementSubjectReference subject);

  @NonNull MaterialStateId requireCurrentState(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId expectedStateId
  );

  @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  );
}
