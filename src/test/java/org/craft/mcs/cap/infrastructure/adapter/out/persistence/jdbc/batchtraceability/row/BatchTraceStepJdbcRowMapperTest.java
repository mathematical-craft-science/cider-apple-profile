package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BatchTraceStepJdbcRowMapperTest {

  @Test
  void mapsSplitStepWithSeveralOutputs() {
    final var source = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-1"
    ));
    final var firstOutput = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-1-a"
    ));
    final var secondOutput = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-1-b"
    ));
    final var step = BatchTraceStep.create(
        WorkspaceId.newId(),
        null,
        BatchTraceOperationType.SPLIT,
        List.of(source),
        List.of(firstOutput, secondOutput),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        "split for two production paths"
    );

    final var row = BatchTraceStepJdbcRowMapper.fromDomain(step);
    final var restored = BatchTraceStepJdbcRowMapper.toDomain(row);

    assertThat(row.subjects).hasSize(3);
    assertThat(restored.operationType()).isEqualTo(BatchTraceOperationType.SPLIT);
    assertThat(restored.inputs()).containsExactly(source);
    assertThat(restored.outputs()).containsExactly(firstOutput, secondOutput);
  }

  @Test
  void mapsSubjectMaterialSnapshot() {
    final var profile = ChemicalProfileAxes.of(
        new AxisValue(
            ChemicalAxis.SUGAR,
            new BigDecimal("1.052"),
            MeasurementValueKind.EXACT,
            null,
            null,
            null,
            null
        ),
        new AxisValue(
            ChemicalAxis.ACIDITY,
            new BigDecimal("5.7"),
            MeasurementValueKind.RANGE_MIDPOINT,
            new BigDecimal("5.2"),
            new BigDecimal("6.2"),
            null,
            null
        ),
        null,
        new AxisValue(
            ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
            new BigDecimal("180"),
            MeasurementValueKind.EXACT,
            null,
            null,
            null,
            null
        )
    );
    final var input = new BatchTraceStepSubject(
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"),
        new BatchTraceMaterialSnapshot(
            VolumeValue.ofLiters("120.5"),
            profile,
            ConfidenceLevel.LAB_VERIFIED
        )
    );
    final var output = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1"
    ));
    final var step = BatchTraceStep.create(
        WorkspaceId.newId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(input),
        List.of(output),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );

    final var restored = BatchTraceStepJdbcRowMapper.toDomain(BatchTraceStepJdbcRowMapper.fromDomain(step));
    final var snapshot = restored.inputs().getFirst().snapshot();

    assertThat(snapshot).isNotNull();
    assertThat(snapshot.volume().liters()).isEqualByComparingTo("120.5");
    assertThat(snapshot.confidence()).isEqualTo(ConfidenceLevel.LAB_VERIFIED);
    assertThat(snapshot.profile().valueOf(ChemicalAxis.SUGAR))
        .get()
        .extracting(AxisValue::value, AxisValue::valueKind)
        .containsExactly(new BigDecimal("1.052"), MeasurementValueKind.EXACT);
    assertThat(snapshot.profile().valueOf(ChemicalAxis.ACIDITY))
        .get()
        .extracting(AxisValue::min, AxisValue::max)
        .containsExactly(new BigDecimal("5.2"), new BigDecimal("6.2"));
    assertThat(snapshot.profile().valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN))
        .get()
        .extracting(AxisValue::value, AxisValue::valueKind)
        .containsExactly(new BigDecimal("180"), MeasurementValueKind.EXACT);
  }

  private static BatchTraceSubject subject(
      BatchTraceSubjectType type,
      String id
  ) {
    return BatchTraceSubject.of(type, id);
  }
}
