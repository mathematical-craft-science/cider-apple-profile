package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Reads current active RESULT records without loading measurement aggregates. */
public interface CurrentMeasurementResultQuery {

  @NonNull List<@NonNull CurrentMeasurementResultView> findBySubjects(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  );
}
