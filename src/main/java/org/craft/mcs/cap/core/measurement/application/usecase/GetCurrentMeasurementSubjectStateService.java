package org.craft.mcs.cap.core.measurement.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.port.in.GetCurrentMeasurementSubjectStateUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMaterialStateProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Returns the subject's current material state for immediate sampling and compatibility flows. */
@Component
@RequiredArgsConstructor
public final class GetCurrentMeasurementSubjectStateService implements GetCurrentMeasurementSubjectStateUseCase {

  private final @NonNull MeasurementSubjectAccessControl subjectAccessControl;
  private final @NonNull CurrentMaterialStateProvider materialStateProvider;

  @Override
  public @NonNull MaterialStateId currentStateOf(@NonNull MeasurementSubjectReference subject) {
    requireNonNull(subject, "measurement subject cannot be null");
    subjectAccessControl.requireReadAccess(subject);
    return materialStateProvider.currentStateOf(subject);
  }
}
