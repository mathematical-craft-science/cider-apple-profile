package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.PredictFeedstockSamplePotentialAbvService;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.domain.fermentation.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.shared.domain.fermentation.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.domain.fermentation.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
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
    final var service = service(repository);
    final var sample = sample(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .build());
    when(repository.findById(sample.sampleId())).thenReturn(Optional.of(sample));

    final var prediction = service.predict(sample.sampleId());

    assertThat(prediction.sampleId()).isEqualTo(sample.sampleId());
    assertThat(prediction.originalGravityValueKind()).isEqualTo(MeasurementValueKind.EXACT);
    assertThat(prediction.prediction().potentialAbv().percent()).isEqualByComparingTo("6.825");
  }

  @Test
  void rejectsSampleWithoutSugar() {
    final var repository = mock(FeedstockSampleRepository.class);
    final var service = service(repository);
    final var sample = sample(ChemicalProfile.builder()
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.2")))
        .build());
    when(repository.findById(sample.sampleId())).thenReturn(Optional.of(sample));

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
      FeedstockSampleRepository repository
  ) {
    return new PredictFeedstockSamplePotentialAbvService(
        repository,
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
        CiderMakerId.of(randomUUID()),
        FeedstockBatchId.newId(),
        FeedstockSampleName.of("Batch A"),
        FeedstockType.APPLE_JUICE,
        profile,
        Instant.parse("2026-01-15T10:00:00Z")
    );
  }
}
