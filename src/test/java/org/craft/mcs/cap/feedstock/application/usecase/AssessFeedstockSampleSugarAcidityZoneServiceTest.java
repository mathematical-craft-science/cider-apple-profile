package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.AssessFeedstockSampleSugarAcidityZoneService;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.FeedstockType;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.RectangleSugarAcidityZoneAssessor;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AssessFeedstockSampleSugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var repository = mock(FeedstockSampleRepository.class);
    final var service = new AssessFeedstockSampleSugarAcidityZoneService(
        repository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularGraphMetadata()
        )
    );
    final var sample = FeedstockSample.create(
        FeedstockSampleId.newId(),
        CiderMakerId.of(randomUUID()),
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

    final var result = service.assess(sample.sampleId());

    assertThat(result.plottable()).isFalse();
    assertThat(result.assessment()).isNull();
    assertThat(result.capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT);
    assertThat(result.capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.ACIDITY);
  }
}
