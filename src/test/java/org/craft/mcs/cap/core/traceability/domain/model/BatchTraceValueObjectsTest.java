package org.craft.mcs.cap.core.traceability.domain.model;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BatchTraceValueObjectsTest {

  @Test
  void createsSubjectWithoutDisplayNameSnapshot() {
    final var subjectId = UUID.fromString("019d89ef-d580-7000-8000-000000000001");

    assertThat(BatchTraceSubject.of(BatchTraceSubjectType.PRODUCTION_BATCH, subjectId))
        .returns(BatchTraceSubjectType.PRODUCTION_BATCH, BatchTraceSubject::type)
        .returns(subjectId, BatchTraceSubject::id)
        .returns(null, BatchTraceSubject::displayNameSnapshot);
  }

  @Test
  void normalizesOptionalSubjectDisplayNameSnapshot() {
    final var subjectId = UUID.fromString("019d89ef-d580-7000-8000-000000000002");
    final var named = new BatchTraceSubject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        subjectId,
        "  Orchard juice  "
    );
    final var unnamed = new BatchTraceSubject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        subjectId,
        "   "
    );

    assertThat(named.displayNameSnapshot()).isEqualTo("Orchard juice");
    assertThat(unnamed.displayNameSnapshot()).isNull();
  }

  @Test
  void capturesImmutableMaterialStateSnapshot() {
    final var materialStateId = MaterialStateId.newId();
    final var subject = BatchTraceSubject.of(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        UUID.fromString("019d89ef-d580-7000-8000-000000000003")
    );
    final var snapshot = BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("92"));

    assertThat(BatchTraceStepSubject.capture(materialStateId, subject, snapshot))
        .returns(materialStateId, BatchTraceStepSubject::materialStateId)
        .returns(subject, BatchTraceStepSubject::subject)
        .returns(snapshot, BatchTraceStepSubject::snapshot)
        .satisfies(value -> assertThat(value.invalidatedCharacteristics()).isEmpty());
  }
}
