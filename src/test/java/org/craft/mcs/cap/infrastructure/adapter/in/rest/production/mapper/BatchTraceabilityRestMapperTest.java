package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceabilityGraphProjector;
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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BatchTraceabilityRestMapperTest {

  @Test
  void mapsTraceStepOccurredAtToGraphEdgeResponse() {
    final var occurredAt = Instant.parse("2026-05-18T00:00:00Z");
    final var step = BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            "production-1"
        ))),
        occurredAt,
        null,
        null
    );

    final var response = BatchTraceabilityRestMapper.toResponse(
        BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(step))
    );

    assertThat(response.edges()).singleElement()
        .satisfies(edge -> assertThat(edge.occurredAt()).isEqualTo(occurredAt));
  }

  @Test
  void mapsYanAxisFromTraceSnapshot() {
    final var step = BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(new BatchTraceStepSubject(
            BatchTraceSubject.of(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"),
            new BatchTraceMaterialSnapshot(null, ChemicalProfileAxes.of(
                null,
                null,
                null,
                new AxisValue(
                    ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
                    new BigDecimal("180"),
                    MeasurementValueKind.CALCULATED,
                    null,
                    null,
                    null,
                    null
                )
            ), null)
        )),
        Instant.parse("2026-05-18T00:00:00Z"),
        null,
        null
    );

    final var response = BatchTraceabilityRestMapper.toResponse(
        BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(step))
    );

    assertThat(response.nodes())
        .filteredOn(node -> node.subject().type() == BatchTraceSubjectType.PRODUCTION_BATCH)
        .singleElement()
        .satisfies(node -> assertThat(node.snapshot().axes()).singleElement()
            .satisfies(axis -> {
              assertThat(axis.axis()).isEqualTo(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN);
              assertThat(axis.value().value()).isEqualByComparingTo("180");
              assertThat(axis.value().unit()).isEqualTo("mg N/L");
              assertThat(axis.valueKind()).isEqualTo(MeasurementValueKind.CALCULATED);
            }));
  }
}
