package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.production.application.model.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.traceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceCharacteristicSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialConsumption;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.production.testing.ProductionSourceCharacteristicsFixtures
    .productionSourceCharacteristics;


class BatchTraceabilityProductionBatchTraceRecorderTest {

  private static final AccountUserId ACTOR_USER_ID = AccountUserId.newId();

  @Test
  void mapsProductionCreationToOneImmutableTraceStep() {
    final var workspaceId = WorkspaceId.newId();
    final var sourceStateId = MaterialStateId.newId();
    final var remainingSourceStateId = MaterialStateId.newId();
    final var sourceId = FeedstockBatchId.newId();
    final var appendUseCase = new CapturingAppendUseCase();
    final var recorder = new BatchTraceabilityProductionBatchTraceRecorder(
        appendUseCase,
        currentAccountProfile()
    );
    final var productionBatch = productionBatch(workspaceId);
    final var occurredAt = Instant.parse("2026-05-18T10:15:30Z");

    recorder.recordCreated(
        productionBatch,
        List.of(new ConsumedProductionSource(
            ProductionSourceReference.feedstockBatch(sourceId.value()),
            ProductionSourceDisplayName.of("Late juice"),
            new MaterialConsumption(
                VolumeValue.ofLiters("250"),
                VolumeValue.ofLiters("100"),
                VolumeValue.ofLiters("150"),
                sourceStateId,
                remainingSourceStateId
            ),
            productionSourceCharacteristics("1.050", "6.1", "1400", "180"),
            ConfidenceLevel.LAB_VERIFIED,
            ProductionSourceEvidenceReference.of("measurement-set")
        )),
        occurredAt,
        "accepted input"
    );

    final var traceStep = appendUseCase.traceStep;
    assertThat(traceStep)
        .isNotNull();
    assertThat(traceStep)
        .returns(BatchTraceStepType.PRODUCTION_BATCH_CREATED, BatchTraceStep::stepType)
        .returns(occurredAt, BatchTraceStep::occurredAt)
        .returns("accepted input", BatchTraceStep::note)
        .returns(ActorReference.accountUser(ACTOR_USER_ID.value()), BatchTraceStep::performedBy);

    final var sourceInput = traceStep.inputs().getFirst();
    final var sourceOutput = outputOfType(traceStep, BatchTraceSubjectType.FEEDSTOCK_BATCH);
    final var productionOutput = outputOfType(traceStep, BatchTraceSubjectType.PRODUCTION_BATCH);
    assertThat(sourceInput)
        .returns(sourceStateId, BatchTraceStepSubject::materialStateId)
        .satisfies(input -> assertThat(input.snapshot().volume().liters())
            .isEqualByComparingTo("250"));
    assertThat(sourceOutput)
        .returns(remainingSourceStateId, BatchTraceStepSubject::materialStateId)
        .satisfies(output -> assertThat(output.snapshot().volume().liters())
            .isEqualByComparingTo("150"))
        .satisfies(output -> assertThat(directValue(
            output.snapshot().characteristics().yeastAssimilableNitrogen()
        )).isEqualTo(PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("180"))));
    assertThat(productionOutput)
        .returns(productionBatch.currentMaterialStateId(), BatchTraceStepSubject::materialStateId)
        .satisfies(output -> assertThat(output.snapshot().volume().liters())
            .isEqualByComparingTo("100"))
        .satisfies(output -> assertThat(output.snapshot().characteristics().specificGravity())
            .isEqualTo(BatchTraceCharacteristicSnapshot.calculated(
                productionBatch.initialProfileSnapshot().specificGravity()
            )));

  }

  @Test
  void preservesIntervalBoundsInSourceTraceSnapshot() {
    final var workspaceId = WorkspaceId.newId();
    final var sourceStateId = MaterialStateId.newId();
    final var remainingSourceStateId = MaterialStateId.newId();
    final var sourceId = FeedstockBatchId.newId();
    final var appendUseCase = new CapturingAppendUseCase();
    final var recorder = new BatchTraceabilityProductionBatchTraceRecorder(
        appendUseCase,
        currentAccountProfile()
    );

    recorder.recordCreated(
        productionBatch(workspaceId),
        List.of(new ConsumedProductionSource(
            ProductionSourceReference.feedstockBatch(sourceId.value()),
            ProductionSourceDisplayName.of("Interval source"),
            new MaterialConsumption(
                VolumeValue.ofLiters("100"),
                VolumeValue.ofLiters("60"),
                VolumeValue.ofLiters("40"),
                sourceStateId,
                remainingSourceStateId
            ),
            new ProductionSourceCharacteristics(
                IntervalCharacteristicValue.of(
                    SpecificGravityValue.of("1.048"),
                    SpecificGravityValue.of("1.052")
                ),
                PointCharacteristicValue.of(TitratableAcidityValue.of("6.1")),
                null,
                null
            ),
            ConfidenceLevel.MEDIUM,
            ProductionSourceEvidenceReference.of("measurement-set")
        )),
        Instant.parse("2026-05-18T10:15:30Z"),
        null
    );

    final var specificGravity = appendUseCase.traceStep.inputs()
        .getFirst()
        .snapshot()
        .characteristics()
        .specificGravity();

    assertThat(directValue(specificGravity)).isEqualTo(IntervalCharacteristicValue.of(
        SpecificGravityValue.of("1.048"),
        SpecificGravityValue.of("1.052")
    ));
  }

  private static @NonNull BatchTraceStepSubject outputOfType(
      @NonNull BatchTraceStep traceStep,
      @NonNull BatchTraceSubjectType subjectType
  ) {
    return traceStep.outputs().stream()
        .filter(output -> output.subject().type() == subjectType)
        .findFirst()
        .orElseThrow();
  }

  private static <T extends MeasuredValue> @NonNull CharacteristicValue<T> directValue(
      @NonNull BatchTraceCharacteristicSnapshot<T> snapshot
  ) {
    return switch (snapshot) {
      case BatchTraceCharacteristicSnapshot.Direct<T>(var value) -> value;
      case BatchTraceCharacteristicSnapshot.Calculated<T> _ ->
          throw new AssertionError("expected direct trace characteristic");
    };
  }

  private static @NonNull ProductionBatch productionBatch(@NonNull WorkspaceId workspaceId) {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        workspaceId,
        BatchName.of("Classic cider"),
        VesselTestFixtures.vesselId("Tank F-03"),
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

  private static final class CapturingAppendUseCase implements AppendBatchTraceStepUseCase {

    private BatchTraceStep traceStep;

    @Override
    public void append(@NonNull BatchTraceStep traceStep) {
      this.traceStep = traceStep;
    }
  }
}
