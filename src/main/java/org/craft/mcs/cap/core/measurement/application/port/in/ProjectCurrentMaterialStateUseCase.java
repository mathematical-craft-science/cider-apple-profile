package org.craft.mcs.cap.core.measurement.application.port.in;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMaterialStateView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Projects measured characteristics that remain applicable to a subject's current material state. */
public interface ProjectCurrentMaterialStateUseCase {

  @NonNull CurrentMaterialStateView project(
      @NonNull MeasurementSubjectReference subject
  );

  @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull CurrentMaterialStateView> projectAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  );
}
