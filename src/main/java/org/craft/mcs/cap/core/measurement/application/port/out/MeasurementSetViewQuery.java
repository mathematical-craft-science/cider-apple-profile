package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Read-side port for current measurement-set revisions without loading mutable aggregates. */
public interface MeasurementSetViewQuery {

  @NonNull Optional<@NonNull MeasurementSetView> findById(
      @NonNull MeasurementSetId measurementSetId
  );

  @NonNull List<@NonNull MeasurementSetView> findBySubject(
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject
  );
}
