package org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.mapper;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.traceability.domain.graph.BatchTraceabilityGraphProjector;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceCharacteristicSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialCharacteristics;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceNodeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
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
  void mapsCalculatedYanCharacteristicFromTraceSnapshot() {
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
            new BatchTraceMaterialSnapshot(null, new BatchTraceMaterialCharacteristics(
                null,
                null,
                null,
                BatchTraceCharacteristicSnapshot.calculated(
                    YeastAssimilableNitrogenValue.of("180")
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
        .satisfies(node -> assertThat(node.snapshot().characteristics()).singleElement()
            .satisfies(characteristic -> {
              assertThat(characteristic.characteristic()).isEqualTo(MeasurementCharacteristic.YAN);
              assertThat(characteristic.value().value().value()).isEqualByComparingTo("180");
              assertThat(characteristic.value().value().unit()).isEqualTo("mg N/L");
              assertThat(characteristic.value().valueKind())
                  .isEqualTo(ProfileCharacteristicValueKind.CALCULATED);
            }));
  }
}
