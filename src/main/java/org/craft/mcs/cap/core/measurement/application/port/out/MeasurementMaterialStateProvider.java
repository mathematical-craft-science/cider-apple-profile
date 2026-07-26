package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Resolves material states owned by measurable business subjects. */
public interface MeasurementMaterialStateProvider {

  @NonNull MaterialStateId currentStateOf(@NonNull MeasurementSubjectReference subject);

  /**
   * Verifies that an immutable state belongs to the supplied business subject.
   *
   * <p>The state does not have to remain current. This permits delayed laboratory results to be
   * attached to the material state that was actually sampled.</p>
   */
  @NonNull MaterialStateId requireObservedState(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId observedStateId
  );

  @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  );
}
