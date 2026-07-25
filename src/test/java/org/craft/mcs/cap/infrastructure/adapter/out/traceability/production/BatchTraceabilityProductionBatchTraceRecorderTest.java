package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.GetCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.InitializeMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class BatchTraceabilityProductionBatchTraceRecorderTest {

  private static final AccountUserId ACTOR_USER_ID = AccountUserId.newId();

  private static ChemicalProfileAxes sourceAxes() {
    return ChemicalProfileAxes.of(
        axis(ChemicalAxis.SUGAR, "1.050"),
        axis(ChemicalAxis.ACIDITY, "6.1"),
        axis(ChemicalAxis.TANNIN, "1400"),
        axis(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, "180")
    );
  }

  @Test
  void mapsProductionCreatedFactToBatchTraceStep() {
    final var workspaceId = WorkspaceId.newId();
    final var sourceStateId = MaterialStateId.newId();
    final var sourceId = FeedstockBatchId.newId().value();
    final var initializeUseCase = new CapturingInitializeUseCase();
    final var recorder = new BatchTraceabilityProductionBatchTraceRecorder(
        initializeUseCase,
        stateQuery(sourceState(
            workspaceId,
            sourceStateId,
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            sourceId,
            "Late juice",
            null,
            null
        )),
        currentAccountProfile()
    );
    final var productionBatch = productionBatch(workspaceId);
    final var occurredAt = Instant.parse("2026-05-18T10:15:30Z");

    recorder.recordCreated(
        productionBatch,
        List.of(new ProductionBatchFeedstockTraceSource(
            FeedstockBatchId.of(sourceId),
            sourceStateId,
            ProductionSourceDisplayName.of("Late juice"),
            VolumeValue.ofLiters("100"),
            sourceAxes(),
            ConfidenceLevel.LAB_VERIFIED
        )),
        occurredAt,
        "accepted input"
    );

    assertThat(initializeUseCase.step)
        .isNotNull()
        .returns(BatchTraceStepType.PRODUCTION_BATCH_CREATED, BatchTraceStep::stepType)
        .returns(occurredAt, BatchTraceStep::occurredAt)
        .returns("accepted input", BatchTraceStep::note)
        .satisfies(step -> assertThat(step.performedBy().id())
            .isEqualTo(ACTOR_USER_ID.value().toString()));
    assertThat(initializeUseCase.step.inputs()).singleElement()
        .satisfies(inputSubject -> {
          assertThat(inputSubject.traceStateId()).isEqualTo(sourceStateId);
          assertThat(inputSubject.subject().type()).isEqualTo(BatchTraceSubjectType.FEEDSTOCK_BATCH);
          assertThat(inputSubject.subject().id()).isEqualTo(sourceId);
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
    assertThat(initializeUseCase.step.outputs()).singleElement()
        .satisfies(outputSubject -> {
          assertThat(outputSubject.subject().type()).isEqualTo(BatchTraceSubjectType.PRODUCTION_BATCH);
          assertThat(outputSubject.subject().id()).isEqualTo(productionBatch.batchId().value());
          assertThat(outputSubject.snapshot().volume().liters()).isEqualByComparingTo("100");
          assertThat(outputSubject.snapshot().profile().valueOf(ChemicalAxis.SUGAR))
              .get()
              .satisfies(value -> assertThat(value.valueKind()).isEqualTo(AxisValueKind.CALCULATED));
          assertThat(outputSubject.snapshot().profile().valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN))
              .get()
              .satisfies(value -> {
                assertThat(value.value()).isEqualByComparingTo("180");
                assertThat(value.valueKind()).isEqualTo(AxisValueKind.CALCULATED);
              });
        });
  }

  private static @NonNull MaterialStateSnapshot sourceState(
      @NonNull WorkspaceId workspaceId,
      @NonNull MaterialStateId stateId,
      @NonNull BatchTraceSubjectType subjectType,
      @NonNull UUID subjectId,
      @NonNull String displayName,
      @Nullable ChemicalProfileAxes profile,
      @Nullable ConfidenceLevel confidence
  ) {
    return new MaterialStateSnapshot(
        workspaceId,
        new BatchTraceStepSubject(
            stateId,
            new BatchTraceSubject(subjectType, subjectId, displayName),
            new BatchTraceMaterialSnapshot(VolumeValue.ofLiters("530"), profile, confidence)
        )
    );
  }

  private static @NonNull GetCurrentMaterialStateUseCase stateQuery(
      @NonNull MaterialStateSnapshot state
  ) {
    final var query = mock(GetCurrentMaterialStateUseCase.class);
    when(query.getState(state.state().traceStateId())).thenReturn(state);
    return query;
  }

  private static AxisValue axis(
      ChemicalAxis axis,
      String value
  ) {
    return new AxisValue(
        axis,
        new BigDecimal(value),
        AxisValueKind.POINT,
        null,
        null
    );
  }

  private static ProductionBatch productionBatch(WorkspaceId workspaceId) {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        workspaceId,
        BatchName.of("Classic cider"),
        Vessel.named("Tank F-03"),
        VolumeValue.ofLiters("100"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("5.8"),
            TanninValue.of("1200"),
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

  private static final class CapturingInitializeUseCase implements InitializeMaterialStateUseCase {

    private BatchTraceStep step;

    @Override
    public void initialize(
        @NonNull BatchTraceStep step,
        @NonNull BatchTraceSubject subject,
        @NonNull MaterialStateId initialStateId
    ) {
      this.step = step;
    }
  }
}
