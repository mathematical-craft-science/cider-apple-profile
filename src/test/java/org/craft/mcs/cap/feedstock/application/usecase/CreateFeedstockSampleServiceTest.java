package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockSampleCommand;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.CreateFeedstockSampleService;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.shared.application.TransactionHelper;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.VolumeValue;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CreateFeedstockSampleServiceTest {

  @Test
  void createsSampleInsideExistingBatchOwnedBySameCiderMaker() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var sampleRepository = mock(FeedstockSampleRepository.class);
    final var service = new CreateFeedstockSampleService(batchRepository, sampleRepository, transactionHelper());
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var batch = batch(ciderMakerId);
    when(batchRepository.findById(batch.batchId())).thenReturn(Optional.of(batch));
    when(sampleRepository.save(any(FeedstockSample.class))).thenAnswer(invocation -> invocation.getArgument(0));

    final var created = service.create(new CreateFeedstockSampleCommand(
        batch.batchId(),
        FeedstockSampleName.of("Late press sample"),
        FeedstockType.APPLE_JUICE,
        chemicalProfile(),
        Instant.parse("2026-10-15T10:00:00Z"),
        null
    ));

    assertThat(created.batchId()).isEqualTo(batch.batchId());
    assertThat(created.ciderMakerId()).isEqualTo(ciderMakerId);
  }

  @Test
  void rejectsSampleWhenBatchDoesNotExist() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new CreateFeedstockSampleService(
        batchRepository,
        mock(FeedstockSampleRepository.class),
        transactionHelper()
    );
    final var batchId = FeedstockBatchId.newId();
    when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(new CreateFeedstockSampleCommand(
        batchId,
        FeedstockSampleName.of("Late press sample"),
        FeedstockType.APPLE_JUICE,
        chemicalProfile(),
        Instant.parse("2026-10-15T10:00:00Z"),
        null
    ))).isInstanceOf(FeedstockBatchNotFoundException.class);
  }

  private static FeedstockBatch batch(CiderMakerId ciderMakerId) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        ciderMakerId,
        FeedstockBatchName.of("Late season fruit"),
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

  private static TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @Override
      public <T> T callInTransactionNonNull(Supplier<T> action) {
        return action.get();
      }

      @Override
      public void executeInTransaction(Runnable action) {
        action.run();
      }
    };
  }
}
