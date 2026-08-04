package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementResultEvidenceView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Reads active Measurement RESULT evidence for explicitly supplied subjects and material states. */
public interface MeasurementResultEvidenceQuery {

  @NonNull List<@NonNull MeasurementResultEvidenceView> findByMaterialStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull Set<@NonNull MaterialStateId>> states
  );
}
