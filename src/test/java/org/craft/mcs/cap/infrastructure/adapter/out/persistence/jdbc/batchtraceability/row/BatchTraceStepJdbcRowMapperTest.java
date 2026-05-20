package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceId;
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
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
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
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.SPLIT,
        List.of(source),
        List.of(firstOutput, secondOutput),
        Instant.parse("2026-05-18T00:00:00Z"),
        "technologist",
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
        null
    );
    final var input = new BatchTraceStepSubject(
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"),
        new BatchTraceMaterialSnapshot(
            VolumeValue.ofLiters("120.5"),
            profile,
            "LAB_VERIFIED"
        )
    );
    final var output = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1"
    ));
    final var step = BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(input),
        List.of(output),
        Instant.parse("2026-05-18T00:00:00Z"),
        null,
        null
    );

    final var restored = BatchTraceStepJdbcRowMapper.toDomain(BatchTraceStepJdbcRowMapper.fromDomain(step));
    final var snapshot = restored.inputs().getFirst().snapshot();

    assertThat(snapshot).isNotNull();
    assertThat(snapshot.volume().liters()).isEqualByComparingTo("120.5");
    assertThat(snapshot.confidence()).isEqualTo("LAB_VERIFIED");
    assertThat(snapshot.profile().valueOf(ChemicalAxis.SUGAR))
        .get()
        .extracting(AxisValue::value, AxisValue::valueKind)
        .containsExactly(new BigDecimal("1.052"), MeasurementValueKind.EXACT);
    assertThat(snapshot.profile().valueOf(ChemicalAxis.ACIDITY))
        .get()
        .extracting(AxisValue::min, AxisValue::max)
        .containsExactly(new BigDecimal("5.2"), new BigDecimal("6.2"));
  }

  private static BatchTraceSubject subject(BatchTraceSubjectType type, String id) {
    return BatchTraceSubject.of(type, id);
  }
}
