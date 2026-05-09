package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.ListFeedstockSampleSugarAcidityZonePointsService;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
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


class ListFeedstockSampleSugarAcidityZonePointsServiceTest {

  @Test
  void returnsPlottablePointsAndUnplottableSamplesWithMissingAxes() {
    final var repository = mock(FeedstockSampleRepository.class);
    final var service = new ListFeedstockSampleSugarAcidityZonePointsService(
        repository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularGraphMetadata()
        )
    );
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var plottable = sample(
        ciderMakerId,
        "Plottable batch",
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.060")
            )))
            .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.5")))
            .build()
    );
    final var unplottable = sample(
        ciderMakerId,
        "Partial batch",
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build()
    );
    when(repository.findAllByCiderMakerId(ciderMakerId)).thenReturn(List.of(plottable, unplottable));

    final var batch = service.listByCiderMakerId(ciderMakerId);

    assertThat(batch.points())
        .extracting(FeedstockSampleSugarAcidityZoneAssessment::sampleId)
        .containsExactly(plottable.sampleId());
    assertThat(batch.unplottable()).hasSize(1);
    assertThat(batch.unplottable().getFirst().sampleId()).isEqualTo(unplottable.sampleId());
    assertThat(batch.unplottable().getFirst().capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT);
    assertThat(batch.unplottable().getFirst().capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.ACIDITY);
  }

  private static FeedstockSample sample(
      CiderMakerId ciderMakerId,
      String name,
      ChemicalProfile profile
  ) {
    return FeedstockSample.create(
        FeedstockSampleId.newId(),
        ciderMakerId,
        FeedstockBatchId.newId(),
        FeedstockSampleName.of(name),
        FeedstockType.APPLE_JUICE,
        profile,
        Instant.parse("2026-01-15T10:00:00Z")
    );
  }
}
