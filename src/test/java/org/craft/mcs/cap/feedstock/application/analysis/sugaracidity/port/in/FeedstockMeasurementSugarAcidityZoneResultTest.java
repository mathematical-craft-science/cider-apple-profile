package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockMeasurementSugarAcidityZoneResultTest {

  @Test
  void rejectsPlottableResultWithoutAssessment() {
    assertThatThrownBy(() -> new FeedstockMeasurementSugarAcidityZoneResult(
            FeedstockMeasurementId.newId(),
            CiderMakerId.of(randomUUID()),
            FeedstockBatchId.newId(),
            FeedstockMeasurementName.of("Partial batch"),
            true,
            null,
            capabilityAssessment()
        ))
        .isInstanceOf(ApplicationValidationException.class);
  }

  @Test
  void allowsUnplottableResultWithoutAssessment() {
    final var result = new FeedstockMeasurementSugarAcidityZoneResult(
        FeedstockMeasurementId.newId(),
        CiderMakerId.of(randomUUID()),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Partial batch"),
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
