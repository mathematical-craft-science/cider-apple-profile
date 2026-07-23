package org.craft.mcs.cap.feedstock.application.measurement.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.UpdateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.usecase.UpdateFeedstockMeasurementService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.TestClocks.FEEDSTOCK_MEASUREMENT_CLOCK;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;


class UpdateFeedstockMeasurementServiceTest {

  @Test
  void mergesPatchChemicalProfileIntoExistingMeasurement() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var measurementRepository = mock(FeedstockMeasurementRepository.class);
    final var service = new UpdateFeedstockMeasurementService(
        batchRepository,
        measurementRepository,
        mock(FeedstockAccessControl.class),
        transactionHelper(),
        FEEDSTOCK_MEASUREMENT_CLOCK
    );
    final var batch = batch();
    final var measurement = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batch.batchId(),
        FeedstockMeasurementName.of("Press sample"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementInstant()
    );

    when(batchRepository.findById(batch.batchId())).thenReturn(Optional.of(batch));
    when(measurementRepository.findVersionedById(measurement.measurementId()))
        .thenReturn(Optional.of(versioned(measurement)));
    when(measurementRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

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
        WorkspaceId.of(newUuid()),
        BatchName.of("Late season fruit"),
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
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
