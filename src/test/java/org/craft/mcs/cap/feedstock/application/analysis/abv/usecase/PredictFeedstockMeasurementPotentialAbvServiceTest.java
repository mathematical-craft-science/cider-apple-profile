package org.craft.mcs.cap.feedstock.application.analysis.abv.usecase;

import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.usecase.PredictFeedstockMeasurementPotentialAbvService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class PredictFeedstockMeasurementPotentialAbvServiceTest {

  @Test
  void predictsPotentialAbvForSampleWithSugar() {
    final var repository = mock(FeedstockMeasurementRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = service(repository, batchRepository);
    final var sample = sample(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .build());
    when(repository.findById(sample.measurementId())).thenReturn(Optional.of(sample));
    when(batchRepository.findById(sample.batchId()))
        .thenReturn(Optional.of(batch(sample.batchId(), CiderMakerId.of(randomUUID()))));

    final var prediction = service.predict(sample.measurementId());

    assertThat(prediction.measurementId()).isEqualTo(sample.measurementId());
    assertThat(prediction.originalGravityValueKind()).isEqualTo(MeasurementValueKind.EXACT);
    assertThat(prediction.prediction().potentialAbv().percent()).isEqualByComparingTo("6.825");
  }

  @Test
  void rejectsSampleWithoutSugar() {
    final var repository = mock(FeedstockMeasurementRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = service(repository, batchRepository);
    final var sample = sample(ChemicalProfile.builder()
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.2")))
        .build());
    when(repository.findById(sample.measurementId())).thenReturn(Optional.of(sample));
    when(batchRepository.findById(sample.batchId()))
        .thenReturn(Optional.of(batch(sample.batchId(), CiderMakerId.of(randomUUID()))));

    assertThatThrownBy(() -> service.predict(sample.measurementId()))
        .isInstanceOfSatisfying(
            ChemicalProfileCapabilityNotMetException.class,
            exception -> {
              assertThat(exception.assessment().capability())
                  .isEqualTo(ChemicalProfileCapability.ABV_PREDICTION);
              assertThat(exception.assessment().missingAxes()).containsExactly(ChemicalAxis.SUGAR);
            }
        );
  }

  private static PredictFeedstockMeasurementPotentialAbvService service(
      FeedstockMeasurementRepository repository,
      FeedstockBatchRepository batchRepository
  ) {
    return new PredictFeedstockMeasurementPotentialAbvService(
        repository,
        batchRepository,
        mock(FeedstockAccessControl.class),
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new PotentialAbvPredictor(
            FixedFinalGravityPredictionStrategy.dryCiderDefault(),
            new StandardGravityDropAbvPredictionStrategy()
        )
    );
  }

  private static FeedstockMeasurement sample(ChemicalProfile profile) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Batch A"),
        profile,
        feedstockMeasurementInstant()
    );
  }

  private static FeedstockBatch batch(
      FeedstockBatchId batchId,
      CiderMakerId ciderMakerId
  ) {
    return FeedstockBatch.create(
        batchId,
        ciderMakerId,
        FeedstockBatchName.of("Test batch"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("100")
    );
  }
}
