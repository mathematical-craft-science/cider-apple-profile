package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchCompositionTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BatchTraceabilityProductionBatchTraceRecorderTest {

  private static ProductionInput productionInput(UUID sourceId) {
    return new ProductionInput(
        ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
            FeedstockBatchId.of(sourceId),
            VolumeValue.ofLiters("100")
        ))),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("5.8"),
            TanninityValue.of("1200"),
            YeastAssimilableNitrogenValue.of("180")
        ),
        null
    );
  }

  @Test
  void mapsCompositionResultInputToTraceSubject() {
    final var appendUseCase = new CapturingAppendUseCase();
    final var recorder = new BatchTraceabilityProductionBatchTraceRecorder(appendUseCase);
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var sourceId = UUID.randomUUID().toString();
    final var input = productionInput(UUID.randomUUID());
    final var productionBatch = productionBatch(ciderMakerId, input);

    recorder.recordCreated(
        productionBatch,
        List.of(new ProductionBatchCompositionTraceSource(
            sourceId,
            "Accepted composition",
            VolumeValue.ofLiters("100")
        )),
        Instant.parse("2026-05-18T10:15:30Z"),
        null
    );

    assertThat(appendUseCase.step.inputs()).singleElement()
        .satisfies(inputSubject -> {
          assertThat(inputSubject.subject().type()).isEqualTo(BatchTraceSubjectType.COMPOSITION_RESULT);
          assertThat(inputSubject.subject().id()).isEqualTo(sourceId);
          assertThat(inputSubject.subject().displayNameSnapshot()).isEqualTo("Accepted composition");
        });
  }

  private static ChemicalProfileAxes sourceAxes() {
    return ChemicalProfileAxes.of(
        axis(ChemicalAxis.SUGAR, "1.050"),
        axis(ChemicalAxis.ACIDITY, "6.1"),
        axis(ChemicalAxis.TANNINITY, "1400"),
        axis(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, "180")
    );
  }

  @Test
  void mapsProductionCreatedFactToBatchTraceStep() {
    final var appendUseCase = new CapturingAppendUseCase();
    final var recorder = new BatchTraceabilityProductionBatchTraceRecorder(appendUseCase);
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var sourceId = UUID.randomUUID();
    final var input = productionInput(sourceId);
    final var productionBatch = productionBatch(ciderMakerId, input);
    final var occurredAt = Instant.parse("2026-05-18T10:15:30Z");

    recorder.recordCreated(
        productionBatch,
        List.of(new ProductionBatchFeedstockTraceSource(
            FeedstockBatchId.of(sourceId),
            "Late juice",
            VolumeValue.ofLiters("100"),
            sourceAxes(),
            ConfidenceLevel.LAB_VERIFIED
        )),
        occurredAt,
        "accepted input"
    );

    assertThat(appendUseCase.step).isNotNull();
    assertThat(appendUseCase.step.operationType()).isEqualTo(BatchTraceOperationType.CREATE_PRODUCTION_BATCH);
    assertThat(appendUseCase.step.occurredAt()).isEqualTo(occurredAt);
    assertThat(appendUseCase.step.note()).isEqualTo("accepted input");
    assertThat(appendUseCase.step.inputs()).singleElement()
        .satisfies(inputSubject -> {
          assertThat(inputSubject.subject().type()).isEqualTo(BatchTraceSubjectType.FEEDSTOCK_BATCH);
          assertThat(inputSubject.subject().id()).isEqualTo(sourceId.toString());
          assertThat(inputSubject.subject().displayNameSnapshot()).isEqualTo("Late juice");
          assertThat(inputSubject.snapshot().volume().liters()).isEqualByComparingTo("100");
          assertThat(inputSubject.snapshot().confidence()).isEqualTo(ConfidenceLevel.LAB_VERIFIED);
          assertThat(inputSubject.snapshot().profile().valueOf(ChemicalAxis.ACIDITY))
              .get()
              .satisfies(value -> assertThat(value.value()).isEqualByComparingTo("6.1"));
          assertThat(inputSubject.snapshot().profile().valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN))
              .get()
              .satisfies(value -> assertThat(value.value()).isEqualByComparingTo("180"));
        });
    assertThat(appendUseCase.step.outputs()).singleElement()
        .satisfies(outputSubject -> {
          assertThat(outputSubject.subject().type()).isEqualTo(BatchTraceSubjectType.PRODUCTION_BATCH);
          assertThat(outputSubject.subject().id()).isEqualTo(productionBatch.batchId().value().toString());
          assertThat(outputSubject.snapshot().volume().liters()).isEqualByComparingTo("100");
          assertThat(outputSubject.snapshot().profile().valueOf(ChemicalAxis.SUGAR))
              .get()
              .satisfies(value -> assertThat(value.valueKind()).isEqualTo(MeasurementValueKind.CALCULATED));
          assertThat(outputSubject.snapshot().profile().valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN))
              .get()
              .satisfies(value -> {
                assertThat(value.value()).isEqualByComparingTo("180");
                assertThat(value.valueKind()).isEqualTo(MeasurementValueKind.CALCULATED);
              });
        });
  }

  private static AxisValue axis(ChemicalAxis axis, String value) {
    return new AxisValue(
        axis,
        new BigDecimal(value),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }

  private static ProductionBatch productionBatch(
      CiderMakerId ciderMakerId,
      ProductionInput input
  ) {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        ciderMakerId,
        ProductionBatchName.of("Classic cider"),
        ProductionBatchKind.CIDER,
        input.sourceComposition().totalVolume(),
        input.profile(),
        null,
        null
    );
  }

  private static final class CapturingAppendUseCase implements AppendBatchTraceStepUseCase {

    private BatchTraceStep step;

    @Override
    public @NonNull BatchTraceStepId append(@NonNull BatchTraceStep step) {
      this.step = step;
      return step.stepId();
    }
  }
}
