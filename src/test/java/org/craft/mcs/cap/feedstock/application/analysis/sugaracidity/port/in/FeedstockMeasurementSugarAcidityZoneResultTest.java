package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.*;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockMeasurementSugarAcidityZoneResultTest {

  @Test
  void rejectsMissingEvaluation() {
    assertThatThrownBy(() -> new FeedstockMeasurementSugarAcidityZoneResult(
            FeedstockMeasurementId.newId(),
            WorkspaceId.of(newUuid()),
            FeedstockBatchId.newId(),
            FeedstockMeasurementName.of("Partial batch"),
            null
        ))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void allowsUnplottableResultWithoutAssessment() {
    final var result = new FeedstockMeasurementSugarAcidityZoneResult(
        FeedstockMeasurementId.newId(),
        WorkspaceId.of(newUuid()),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Partial batch"),
        evaluation()
    );

    assertThat(result.evaluation().plottable()).isFalse();
    assertThat(result.evaluation().assessment()).isNull();
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

  private static SugarAcidityProfileEvaluation evaluation() {
    final var axes = new ChemicalProfileAxisExtractor().extract(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build());
    return new SugarAcidityProfileEvaluation(axes, capabilityAssessment(), false, null);
  }
}
