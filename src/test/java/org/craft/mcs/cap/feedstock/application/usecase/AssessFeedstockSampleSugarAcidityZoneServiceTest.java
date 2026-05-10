package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.AssessFeedstockSampleSugarAcidityZoneService;
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
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.RectangleSugarAcidityZoneAssessor;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AssessFeedstockSampleSugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var repository = mock(FeedstockSampleRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new AssessFeedstockSampleSugarAcidityZoneService(
        repository,
        batchRepository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularGraphMetadata()
        )
    );
    final var sample = FeedstockSample.create(
        FeedstockSampleId.newId(),
        FeedstockBatchId.newId(),
        FeedstockSampleName.of("Partial batch"),
        FeedstockType.APPLE_JUICE,
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build(),
        Instant.parse("2026-01-15T10:00:00Z")
    );
    when(repository.findById(sample.sampleId())).thenReturn(Optional.of(sample));
    when(batchRepository.findById(sample.batchId()))
        .thenReturn(Optional.of(batch(sample.batchId(), CiderMakerId.of(randomUUID()))));

    final var result = service.assess(sample.sampleId());

    assertThat(result.plottable()).isFalse();
    assertThat(result.assessment()).isNull();
    assertThat(result.capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT);
    assertThat(result.capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.ACIDITY);
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
