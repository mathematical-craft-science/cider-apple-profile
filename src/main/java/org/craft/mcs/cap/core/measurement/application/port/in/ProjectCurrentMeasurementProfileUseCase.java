package org.craft.mcs.cap.core.measurement.application.port.in;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementProfileView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Adapts state-aware current evidence to the transitional ChemicalProfile representation. */
public interface ProjectCurrentMeasurementProfileUseCase {

  @NonNull Optional<@NonNull CurrentMeasurementProfileView> project(
      @NonNull MeasurementSubjectReference subject
  );

  /**
   * Projects current profiles for several subjects while allowing the read adapter to batch the query.
   */
  @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull CurrentMeasurementProfileView> projectAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  );
}
