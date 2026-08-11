package org.craft.mcs.cap.core.measurement.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetNotFoundException;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectChangePolicy;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Loads a measurement aggregate after enforcing subject write access and expected version. */
@Component
@RequiredArgsConstructor
final class MeasurementSetWriteAccess {

  private final @NonNull MeasurementSetRepository repository;
  private final @NonNull MeasurementSubjectAccessControl subjectAccessControl;
  private final @NonNull MeasurementSubjectChangePolicy subjectChangePolicy;

  @NonNull VersionedAggregate<MeasurementSet> require(
      @NonNull MeasurementSetId measurementSetId,
      long expectedVersion
  ) {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    final var state = repository.findVersionedById(measurementSetId)
        .orElseThrow(() -> new MeasurementSetNotFoundException(measurementSetId));
    subjectAccessControl.requireWriteAccess(state.aggregate().subject());
    subjectChangePolicy.requireChangesAllowed(state.aggregate().subject());
    if (state.revision().value() != expectedVersion) {
      throw new ConcurrentResourceModificationException("measurement set", null);
    }
    return state;
  }
}
