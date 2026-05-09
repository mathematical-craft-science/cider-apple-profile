package org.craft.mcs.cap.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class FeedstockSampleSugarAcidityZoneResultTest {

  @Test
  void rejectsPlottableResultWithoutAssessment() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new FeedstockSampleSugarAcidityZoneResult(
            FeedstockSampleId.newId(),
            CiderMakerId.of(randomUUID()),
            FeedstockBatchId.newId(),
            FeedstockSampleName.of("Partial batch"),
            true,
            null,
            capabilityAssessment()
        ));
  }

  @Test
  void allowsUnplottableResultWithoutAssessment() {
    final var result = new FeedstockSampleSugarAcidityZoneResult(
        FeedstockSampleId.newId(),
        CiderMakerId.of(randomUUID()),
        FeedstockBatchId.newId(),
        FeedstockSampleName.of("Partial batch"),
        false,
        null,
        capabilityAssessment()
    );

    assertThat(result.plottable()).isFalse();
    assertThat(result.assessment()).isNull();
  }

  private static ChemicalProfileCapabilityAssessment capabilityAssessment() {
    return new ChemicalProfileCapabilityAssessor().assess(
        new ChemicalProfileAxisExtractor().extract(ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build()),
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );
  }
}
