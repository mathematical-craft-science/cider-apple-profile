package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.port.out.ObservedMaterialStateValidator;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.traceability.application.port.in.GetMaterialStateUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Adapts immutable Traceability states to Measurement's observed-state validation port. */
@Component
@RequiredArgsConstructor
public final class TraceabilityObservedMaterialStateValidator implements ObservedMaterialStateValidator {

  private final @NonNull GetMaterialStateUseCase materialStateHistory;

  @Override
  public @NonNull MaterialStateId requireObservedState(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId observedStateId
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    requireNonNull(observedStateId, "observed material state id cannot be null");
    final var observedSubject = materialStateHistory.get(observedStateId).state().subject();
    final var expectedSubject = toTraceSubject(subject);
    if (observedSubject.type() != expectedSubject.type()
        || !observedSubject.id().equals(expectedSubject.id())) {
      throw new ApplicationValidationException(
          "observed material state does not belong to the measurement subject"
      );
    }
    return observedStateId;
  }

  private static @NonNull BatchTraceSubject toTraceSubject(
      @NonNull MeasurementSubjectReference subject
  ) {
    return BatchTraceSubject.of(
        switch (subject.type()) {
          case FEEDSTOCK_BATCH -> BatchTraceSubjectType.FEEDSTOCK_BATCH;
          case PRODUCTION_BATCH -> BatchTraceSubjectType.PRODUCTION_BATCH;
        },
        subject.id()
    );
  }
}
