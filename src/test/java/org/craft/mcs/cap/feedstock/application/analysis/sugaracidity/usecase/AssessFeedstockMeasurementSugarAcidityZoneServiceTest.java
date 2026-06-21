package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.usecase;

import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase.AssessFeedstockMeasurementSugarAcidityZoneService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AssessFeedstockMeasurementSugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var repository = mock(FeedstockMeasurementRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new AssessFeedstockMeasurementSugarAcidityZoneService(
        repository,
        batchRepository,
        projector()
    );
    final var sample = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Partial batch"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build(),
        feedstockMeasurementInstant()
    );
    when(repository.findById(sample.measurementId())).thenReturn(Optional.of(sample));
    when(batchRepository.findById(sample.batchId()))
        .thenReturn(Optional.of(batch(sample.batchId(), CiderMakerId.of(randomUUID()))));

    final var result = service.assess(sample.measurementId());

    assertThat(result.plottable()).isFalse();
    assertThat(result.assessment()).isNull();
    assertThat(result.capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT);
    assertThat(result.capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.ACIDITY);
  }

  private static SugarAcidityProfileProjector projector() {
    return new SugarAcidityProfileProjector(
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        )
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
        FeedstockType.APPLE_JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("100")
    );
  }
}
