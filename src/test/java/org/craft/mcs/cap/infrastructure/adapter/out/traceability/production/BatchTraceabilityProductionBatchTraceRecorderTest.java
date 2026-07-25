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
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateAdvance;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateInitialization;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStatesTransition;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.TransitionMaterialStatesUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BatchTraceabilityProductionBatchTraceRecorderTest {

  private static final AccountUserId ACTOR_USER_ID = AccountUserId.newId();

  @Test
  void mapsProductionCreationToSourceAdvanceAndProductionInitialization() {
    final var workspaceId = WorkspaceId.newId();
    final var sourceStateId = MaterialStateId.newId();
    final var sourceId = FeedstockBatchId.newId();
    final var transitionUseCase = new CapturingTransitionUseCase();
    final var recorder = new BatchTraceabilityProductionBatchTraceRecorder(
        transitionUseCase,
        currentAccountProfile()
    );
    final var productionBatch = productionBatch(workspaceId);
    final var occurredAt = Instant.parse("2026-05-18T10:15:30Z");

    recorder.recordCreated(
        productionBatch,
        List.of(new ProductionBatchFeedstockTraceSource(
            sourceId,
            sourceStateId,
            ProductionSourceDisplayName.of("Late juice"),
            VolumeValue.ofLiters("250"),
            VolumeValue.ofLiters("100"),
            VolumeValue.ofLiters("150"),
            sourceAxes(),
            ConfidenceLevel.LAB_VERIFIED
        )),
        occurredAt,
        "accepted input"
    );

    final var transition = transitionUseCase.transition;
    assertThat(transition)
        .isNotNull();
    assertThat(transition.traceStep())
        .returns(BatchTraceStepType.PRODUCTION_BATCH_CREATED, BatchTraceStep::stepType)
        .returns(occurredAt, BatchTraceStep::occurredAt)
        .returns("accepted input", BatchTraceStep::note)
        .satisfies(step -> assertThat(step.performedBy().id())
            .isEqualTo(ACTOR_USER_ID.value().toString()));

    final var sourceInput = transition.traceStep().inputs().getFirst();
    final var sourceOutput = outputOfType(transition, BatchTraceSubjectType.FEEDSTOCK_BATCH);
    final var productionOutput = outputOfType(transition, BatchTraceSubjectType.PRODUCTION_BATCH);
    assertThat(sourceInput)
        .returns(sourceStateId, BatchTraceStepSubject::traceStateId)
        .satisfies(input -> assertThat(input.snapshot().volume().liters())
            .isEqualByComparingTo("250"));
    assertThat(sourceOutput)
        .satisfies(output -> assertThat(output.snapshot().volume().liters())
            .isEqualByComparingTo("150"))
        .satisfies(output -> assertThat(output.snapshot().profile()
            .valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN))
            .get()
            .returns(new BigDecimal("180"), AxisValue::value));
    assertThat(productionOutput)
        .satisfies(output -> assertThat(output.snapshot().volume().liters())
            .isEqualByComparingTo("100"))
        .satisfies(output -> assertThat(output.snapshot().profile().valueOf(ChemicalAxis.SUGAR))
            .get()
            .returns(AxisValueKind.CALCULATED, AxisValue::valueKind));

    assertThat(transition.advances()).singleElement()
        .satisfies(advance -> assertThat(advance)
            .returns(sourceStateId, MaterialStateAdvance::expectedStateId)
            .returns(sourceOutput.traceStateId(), MaterialStateAdvance::nextStateId));
    assertThat(transition.initializations()).singleElement()
        .satisfies(initialization -> assertThat(initialization)
            .returns(productionOutput.traceStateId(), MaterialStateInitialization::initialStateId)
            .satisfies(created -> assertThat(created.subject().id())
                .isEqualTo(productionBatch.batchId().value())));
  }

  private static @NonNull BatchTraceStepSubject outputOfType(
      @NonNull MaterialStatesTransition transition,
      @NonNull BatchTraceSubjectType subjectType
  ) {
    return transition.traceStep().outputs().stream()
        .filter(output -> output.subject().type() == subjectType)
        .findFirst()
        .orElseThrow();
  }

  private static @NonNull ChemicalProfileAxes sourceAxes() {
    return ChemicalProfileAxes.of(
        axis(ChemicalAxis.SUGAR, "1.050"),
        axis(ChemicalAxis.ACIDITY, "6.1"),
        axis(ChemicalAxis.TANNIN, "1400"),
        axis(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, "180")
    );
  }

  private static @NonNull AxisValue axis(
      @NonNull ChemicalAxis axis,
      @NonNull String value
  ) {
    return new AxisValue(
        axis,
        new BigDecimal(value),
        AxisValueKind.POINT,
        null,
        null
    );
  }

  private static @NonNull ProductionBatch productionBatch(@NonNull WorkspaceId workspaceId) {
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

  private static @NonNull GetCurrentAccountProfileUseCase currentAccountProfile() {
    return () -> new AccountUserView(
        ACTOR_USER_ID,
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("Technologist"),
        null,
        AccountUserStatus.ACTIVE
    );
  }

  private static final class CapturingTransitionUseCase implements TransitionMaterialStatesUseCase {

    private MaterialStatesTransition transition;

    @Override
    public void transition(@NonNull MaterialStatesTransition materialStatesTransition) {
      transition = materialStatesTransition;
    }
  }
}
