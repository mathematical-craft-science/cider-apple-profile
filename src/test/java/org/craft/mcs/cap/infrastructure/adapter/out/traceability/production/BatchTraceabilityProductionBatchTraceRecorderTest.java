package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchCompositionTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
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
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BatchTraceabilityProductionBatchTraceRecorderTest {

  private static final AccountUserId ACTOR_USER_ID = AccountUserId.newId();

  @Test
  void mapsCompositionResultInputToTraceSubject() {
    final var appendUseCase = new CapturingAppendUseCase();
    final var recorder = new BatchTraceabilityProductionBatchTraceRecorder(
        appendUseCase,
        currentAccountProfile()
    );
    final var workspaceId = WorkspaceId.newId();
    final var sourceId = UuidV7Generator.newUuid().toString();
    final var productionBatch = productionBatch(workspaceId);

    recorder.recordCreated(
        productionBatch,
        List.of(new ProductionBatchCompositionTraceSource(
            sourceId,
            ProductionSourceDisplayName.of("Accepted composition"),
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
    final var recorder = new BatchTraceabilityProductionBatchTraceRecorder(
        appendUseCase,
        currentAccountProfile()
    );
    final var workspaceId = WorkspaceId.newId();
    final var sourceId = FeedstockBatchId.newId().value();
    final var productionBatch = productionBatch(workspaceId);
    final var occurredAt = Instant.parse("2026-05-18T10:15:30Z");

    recorder.recordCreated(
        productionBatch,
        List.of(new ProductionBatchFeedstockTraceSource(
            FeedstockBatchId.of(sourceId),
            ProductionSourceDisplayName.of("Late juice"),
            VolumeValue.ofLiters("100"),
            sourceAxes(),
            ConfidenceLevel.LAB_VERIFIED
        )),
        occurredAt,
        "accepted input"
    );

    assertThat(appendUseCase.step)
        .isNotNull()
        .returns(BatchTraceOperationType.CREATE_PRODUCTION_BATCH, BatchTraceStep::operationType)
        .returns(occurredAt, BatchTraceStep::occurredAt)
        .returns("accepted input", BatchTraceStep::note)
        .satisfies(step -> assertThat(step.performedBy().id())
            .isEqualTo(ACTOR_USER_ID.value().toString()));
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

  private static AxisValue axis(
      ChemicalAxis axis,
      String value
  ) {
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

  private static ProductionBatch productionBatch(WorkspaceId workspaceId) {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        workspaceId,
        ProductionBatchName.of("Classic cider"),
        ProductionBatchKind.CIDER,
        VolumeValue.ofLiters("100"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("5.8"),
            TanninityValue.of("1200"),
            YeastAssimilableNitrogenValue.of("180")
        ),
        null
    );
  }

  private static GetCurrentAccountProfileUseCase currentAccountProfile() {
    return () -> new AccountUserView(
        ACTOR_USER_ID,
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("Technologist"),
        null,
        AccountUserStatus.ACTIVE
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
