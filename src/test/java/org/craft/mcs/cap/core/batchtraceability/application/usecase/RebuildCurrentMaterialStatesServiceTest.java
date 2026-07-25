package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class RebuildCurrentMaterialStatesServiceTest {

  @Test
  void authorizesAndReplacesProjectionWithReconstructedStates() {
    final var queryStore = mock(BatchTraceabilityQueryStore.class);
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var accessControl = mock(BatchTraceabilityAccessControl.class);
    final var service = new RebuildCurrentMaterialStatesService(
        queryStore,
        currentStateStore,
        accessControl
    );
    final var workspaceId = WorkspaceId.newId();
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var output = new BatchTraceStepSubject(
        MaterialStateId.newId(),
        feedstock,
        BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("100"))
    );
    final var step = BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(output),
        Instant.parse("2026-07-25T01:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );
    when(queryStore.findAllByWorkspace(workspaceId)).thenReturn(List.of(step));

    final var result = service.rebuild(workspaceId);

    final ArgumentCaptor<List<CurrentMaterialState>> statesCaptor = ArgumentCaptor.captor();
    verify(accessControl).requireWriteAccess(
        workspaceId,
        Set.copyOf(EnumSet.allOf(BatchTraceSubjectType.class))
    );
    verify(currentStateStore).replaceAll(eq(workspaceId), statesCaptor.capture());
    assertThat(result.rebuiltSubjectCount()).isOne();
    assertThat(statesCaptor.getValue())
        .singleElement()
        .returns(output.traceStateId(), CurrentMaterialState::materialStateId)
        .returns(0L, CurrentMaterialState::version);
  }
}
