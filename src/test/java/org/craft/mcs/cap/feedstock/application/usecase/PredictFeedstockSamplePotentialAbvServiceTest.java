package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.PredictFeedstockSamplePotentialAbvService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.domain.fermentation.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.shared.domain.fermentation.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.domain.fermentation.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class PredictFeedstockSamplePotentialAbvServiceTest {

  @Test
  void predictsPotentialAbvForSampleWithSugar() {
    final var repository = mock(FeedstockSampleRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = service(repository, batchRepository);
    final var sample = sample(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .build());
    when(repository.findById(sample.sampleId())).thenReturn(Optional.of(sample));
    when(batchRepository.findById(sample.batchId()))
        .thenReturn(Optional.of(batch(sample.batchId(), CiderMakerId.of(randomUUID()))));

    final var prediction = service.predict(sample.sampleId());

    assertThat(prediction.sampleId()).isEqualTo(sample.sampleId());
    assertThat(prediction.originalGravityValueKind()).isEqualTo(MeasurementValueKind.EXACT);
    assertThat(prediction.prediction().potentialAbv().percent()).isEqualByComparingTo("6.825");
  }

  @Test
  void rejectsSampleWithoutSugar() {
    final var repository = mock(FeedstockSampleRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = service(repository, batchRepository);
    final var sample = sample(ChemicalProfile.builder()
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.2")))
        .build());
    when(repository.findById(sample.sampleId())).thenReturn(Optional.of(sample));
    when(batchRepository.findById(sample.batchId()))
        .thenReturn(Optional.of(batch(sample.batchId(), CiderMakerId.of(randomUUID()))));

    assertThatThrownBy(() -> service.predict(sample.sampleId()))
        .isInstanceOfSatisfying(
            ChemicalProfileCapabilityNotMetException.class,
            exception -> {
              assertThat(exception.assessment().capability())
                  .isEqualTo(ChemicalProfileCapability.ABV_PREDICTION);
              assertThat(exception.assessment().missingAxes()).containsExactly(ChemicalAxis.SUGAR);
            }
        );
  }

  private static PredictFeedstockSamplePotentialAbvService service(
      FeedstockSampleRepository repository,
      FeedstockBatchRepository batchRepository
  ) {
    return new PredictFeedstockSamplePotentialAbvService(
        repository,
        batchRepository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new PotentialAbvPredictor(
            FixedFinalGravityPredictionStrategy.dryCiderDefault(),
            new StandardGravityDropAbvPredictionStrategy()
        )
    );
  }

  private static FeedstockSample sample(ChemicalProfile profile) {
    return FeedstockSample.create(
        FeedstockSampleId.newId(),
        FeedstockBatchId.newId(),
        FeedstockSampleName.of("Batch A"),
        FeedstockType.APPLE_JUICE,
        profile,
        Instant.parse("2026-01-15T10:00:00Z")
    );
  }

  private static FeedstockBatch batch(FeedstockBatchId batchId, CiderMakerId ciderMakerId) {
    return FeedstockBatch.create(
        batchId,
        ciderMakerId,
        FeedstockBatchName.of("Test batch"),
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("100")
    );
  }
}
