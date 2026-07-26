package org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.traceability.domain.graph.BatchTraceabilityGraphProjector;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceNodeResponse;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;


class BatchTraceabilityRestMapperTest {

  @Test
  void mapsTraceStepOccurredAtToGraphEdgeResponse() {
    final var occurredAt = Instant.parse("2026-05-18T00:00:00Z");
    final var step = BatchTraceStep.create(
        WorkspaceId.newId(),
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(BatchTraceStepSubject.reference(MaterialStateId.newId(), subject(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(BatchTraceStepSubject.reference(MaterialStateId.newId(), subject(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            "production-1"
        ))),
        occurredAt,
        BatchTraceActorReference.system("test"),
        null
    );

    final var response = BatchTraceabilityRestMapper.toResponse(
        BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(step))
    );

    assertThat(response.edges()).singleElement()
        .satisfies(edge -> {
          assertThat(edge.occurredAt()).isEqualTo(occurredAt);
          assertThat(edge.inputs()).singleElement()
              .satisfies(input -> assertThat(response.nodes())
                  .extracting(BatchTraceNodeResponse::traceStateId)
                  .contains(input.traceStateId()));
          assertThat(edge.outputs()).singleElement()
              .satisfies(output -> assertThat(response.nodes())
                  .extracting(BatchTraceNodeResponse::traceStateId)
                  .contains(output.traceStateId()));
        });
  }

  @Test
  void mapsYanAxisFromTraceSnapshot() {
    final var step = BatchTraceStep.create(
        WorkspaceId.newId(),
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(BatchTraceStepSubject.reference(MaterialStateId.newId(), subject(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(BatchTraceStepSubject.capture(
            MaterialStateId.newId(),
            subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"),
            new BatchTraceMaterialSnapshot(null, ChemicalProfileAxes.of(
                null,
                null,
                null,
                new AxisValue(
                    ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
                    new BigDecimal("180"),
                    AxisValueKind.CALCULATED,
                    null,
                    null
                )
            ), null)
        )),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
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
              assertThat(axis.valueKind()).isEqualTo(AxisValueKind.CALCULATED);
            }));
  }
}
