package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceCharacteristicSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialCharacteristics;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.BatchTraceSubjects;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BatchTraceStepJdbcRowMapperTest {

  @Test
  void mapsProductionCreationStepWithSeveralInputs() {
    final var firstSource = BatchTraceStepSubject.reference(MaterialStateId.newId(), subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-1"
    ));
    final var secondSource = BatchTraceStepSubject.reference(MaterialStateId.newId(), subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-2"
    ));
    final var output = BatchTraceStepSubject.reference(MaterialStateId.newId(), subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1"
    ));
    final var step = BatchTraceStep.create(
        WorkspaceId.newId(),
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(firstSource, secondSource),
        List.of(output),
        Instant.parse("2026-05-18T00:00:00Z"),
        ActorReference.machineIdentity(BatchTraceSubjects.id("test-actor")),
        null
    );

    final var row = BatchTraceStepJdbcRowMapper.fromDomain(step);
    final var restored = BatchTraceStepJdbcRowMapper.toDomain(row);

    assertThat(row.subjects).hasSize(3);
    assertThat(restored.stepType()).isEqualTo(BatchTraceStepType.PRODUCTION_BATCH_CREATED);
    assertThat(restored.inputs()).containsExactly(firstSource, secondSource);
    assertThat(restored.outputs()).containsExactly(output);
  }

  @Test
  void mapsSubjectMaterialSnapshot() {
    final var characteristics = new BatchTraceMaterialCharacteristics(
        BatchTraceCharacteristicSnapshot.direct(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.052"))
        ),
        BatchTraceCharacteristicSnapshot.direct(
            IntervalCharacteristicValue.of(
                TitratableAcidityValue.of("5.2"),
                TitratableAcidityValue.of("6.2")
            )
        ),
        BatchTraceCharacteristicSnapshot.calculated(TanninValue.of("1200")),
        BatchTraceCharacteristicSnapshot.direct(
            PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("180"))
        )
    );
    final var input = BatchTraceStepSubject.capture(
        MaterialStateId.newId(),
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"),
        new BatchTraceMaterialSnapshot(
            VolumeValue.ofLiters("120.5"),
            characteristics,
            ConfidenceLevel.LAB_VERIFIED
        )
    );
    final var output = BatchTraceStepSubject.reference(MaterialStateId.newId(), subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1"
    ));
    final var step = BatchTraceStep.create(
        WorkspaceId.newId(),
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(input),
        List.of(output),
        Instant.parse("2026-05-18T00:00:00Z"),
        ActorReference.machineIdentity(BatchTraceSubjects.id("test-actor")),
        null
    );

    final var restored = BatchTraceStepJdbcRowMapper.toDomain(BatchTraceStepJdbcRowMapper.fromDomain(step));
    final var snapshot = restored.inputs().getFirst().snapshot();

    assertThat(snapshot)
        .isNotNull()
        .returns(ConfidenceLevel.LAB_VERIFIED, BatchTraceMaterialSnapshot::confidence)
        .returns(characteristics, BatchTraceMaterialSnapshot::characteristics)
        .satisfies(value -> assertThat(value.volume().liters()).isEqualByComparingTo("120.5"));
  }

  private static BatchTraceSubject subject(
      BatchTraceSubjectType type,
      String fixtureReference
  ) {
    return BatchTraceSubjects.subject(type, fixtureReference);
  }
}
