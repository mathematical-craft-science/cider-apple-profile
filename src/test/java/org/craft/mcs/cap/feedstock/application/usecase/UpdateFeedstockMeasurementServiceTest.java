package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.port.in.UpdateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.UpdateFeedstockMeasurementService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class UpdateFeedstockMeasurementServiceTest {

  @Test
  void mergesPatchChemicalProfileIntoExistingMeasurement() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var measurementRepository = mock(FeedstockMeasurementRepository.class);
    final var service = new UpdateFeedstockMeasurementService(
        batchRepository,
        measurementRepository,
        transactionHelper()
    );
    final var batch = batch();
    final var measurement = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batch.batchId(),
        FeedstockMeasurementName.of("Press sample"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
            .build(),
        feedstockMeasurementInstant()
    );

    when(batchRepository.findById(batch.batchId())).thenReturn(Optional.of(batch));
    when(measurementRepository.findById(measurement.measurementId())).thenReturn(Optional.of(measurement));
    when(measurementRepository.save(any(FeedstockMeasurement.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.update(new UpdateFeedstockMeasurementCommand(
        batch.batchId(),
        measurement.measurementId(),
        null,
        ChemicalProfile.builder()
            .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.40")))
            .build(),
        true,
        null,
        null,
        false
    ));

    assertThat(measurement.chemicalProfile().sugar()).isPresent();
    assertThat(measurement.chemicalProfile().titratableAcidity()).isPresent();
    assertThat(measurement.chemicalProfile().titratableAcidity().orElseThrow())
        .isInstanceOfSatisfying(ExactMeasurement.class, exact ->
            assertThat(((TitratableAcidityValue) exact.value()).asBigDecimal()).isEqualByComparingTo("5.40")
        );
  }

  private static @NonNull FeedstockBatch batch() {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        CiderMakerId.of(randomUUID()),
        FeedstockBatchName.of("Late season fruit"),
        FeedstockType.APPLE_JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250.000")
    );
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
