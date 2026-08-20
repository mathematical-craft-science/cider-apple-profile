package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateSnapshot;
import org.craft.mcs.cap.core.traceability.application.port.in.GetMaterialStateUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class TraceabilityObservedMaterialStateValidatorTest {

  @Test
  void acceptsHistoricalStateBelongingToMeasurementSubject() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var history = mock(GetMaterialStateUseCase.class);
    when(history.get(stateId)).thenReturn(snapshot(
        stateId,
        BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, subject.id())
    ));

    assertThat(new TraceabilityObservedMaterialStateValidator(history).requireObservedState(subject, stateId))
        .isEqualTo(stateId);
  }

  @Test
  void rejectsHistoricalStateBelongingToAnotherSubject() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var history = mock(GetMaterialStateUseCase.class);
    when(history.get(stateId)).thenReturn(snapshot(
        stateId,
        BatchTraceSubject.of(BatchTraceSubjectType.PRODUCTION_BATCH, newUuid())
    ));
    final var validator = new TraceabilityObservedMaterialStateValidator(history);

    assertThatThrownBy(() -> validator.requireObservedState(subject, stateId))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("observed material state does not belong to the measurement subject");
  }

  private static @NonNull MaterialStateSnapshot snapshot(
      @NonNull MaterialStateId stateId,
      @NonNull BatchTraceSubject subject
  ) {
    return new MaterialStateSnapshot(
        WorkspaceId.newId(),
        BatchTraceStepSubject.reference(stateId, subject)
    );
  }
}
