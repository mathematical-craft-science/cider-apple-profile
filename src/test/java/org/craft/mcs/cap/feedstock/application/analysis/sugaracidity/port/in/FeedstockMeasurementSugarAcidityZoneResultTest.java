package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.*;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;
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

  private static ChemicalProfileAssessment evaluation() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();
    return chemicalProfileAssessmentAdapter().project(profile);
  }
}
