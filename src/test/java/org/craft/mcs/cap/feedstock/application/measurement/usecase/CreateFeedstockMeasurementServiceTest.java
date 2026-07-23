package org.craft.mcs.cap.feedstock.application.measurement.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.CreateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.usecase.CreateFeedstockMeasurementService;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.TestClocks.FEEDSTOCK_MEASUREMENT_CLOCK;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CreateFeedstockMeasurementServiceTest {

  @Test
  void createsSampleInsideExistingBatchOwnedBySameWorkspace() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var sampleRepository = mock(FeedstockMeasurementRepository.class);
    final var service = new CreateFeedstockMeasurementService(
        batchRepository,
        sampleRepository,
        mock(FeedstockAccessControl.class),
        transactionHelper(),
        FEEDSTOCK_MEASUREMENT_CLOCK
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId);
    when(batchRepository.findById(batch.batchId())).thenReturn(Optional.of(batch));
    when(sampleRepository.create(any(FeedstockMeasurement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final var created = service.create(new CreateFeedstockMeasurementCommand(
        batch.batchId(),
        FeedstockMeasurementName.of("Late press sample"),
        chemicalProfile(),
        feedstockMeasurementInstant(),
        null
    ));

    assertThat(created.batchId()).isEqualTo(batch.batchId());
    assertThat(created.workspaceId()).isEqualTo(workspaceId);
  }

  @Test
  void rejectsSampleWhenBatchDoesNotExist() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new CreateFeedstockMeasurementService(
        batchRepository,
        mock(FeedstockMeasurementRepository.class),
        mock(FeedstockAccessControl.class),
        transactionHelper(),
        FEEDSTOCK_MEASUREMENT_CLOCK
    );
    final var batchId = FeedstockBatchId.newId();
    when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(new CreateFeedstockMeasurementCommand(
        batchId,
        FeedstockMeasurementName.of("Late press sample"),
        chemicalProfile(),
        feedstockMeasurementInstant(),
        null
    ))).isInstanceOf(FeedstockBatchNotFoundException.class);
  }

  private static FeedstockBatch batch(WorkspaceId workspaceId) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        workspaceId,
        BatchName.of("Late season fruit"),
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250.000")
    );
  }

  private static ChemicalProfile chemicalProfile() {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .build();
  }

  @NonNull
  private static TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @NonNull
      @Override
      public <T> T callInTransactionNonNull(@NonNull Supplier<T> action) {
        return action.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable action) {
        action.run();
      }
    };
  }
}
