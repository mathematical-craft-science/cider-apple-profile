package org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain;

import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class SugarAcidityProfileProjectorTest {

  private final SugarAcidityProfileProjector projector = new SugarAcidityProfileProjector(
      new ChemicalProfileAxisExtractor(),
      new ChemicalProfileCapabilityAssessor(),
      new RectangleSugarAcidityZoneAssessor(
          BuiltInSugarAcidityZoneProfiles.thresholds(),
          BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
      )
  );

  @Test
  void projectsCompleteChemicalProfileToSugarAcidityPointAndZoneAssessment() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.060")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("4.50")))
        .build();

    final var evaluation = projector.project(profile);

    assertThat(evaluation.plottable()).isTrue();
    assertThat(evaluation.capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT);
    assertThat(evaluation.point()).hasValueSatisfying(point -> {
      assertThat(point.sugar().value()).isEqualByComparingTo("1.060");
      assertThat(point.acidity().value()).isEqualByComparingTo("4.50");
    });
    assertThat(evaluation.assessment()).isNotNull();
    assertThat(evaluation.assessment().zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void returnsEmptyPointWhenChemicalProfileIsMissingRequiredAxis() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();

    final var evaluation = projector.project(profile);

    assertThat(evaluation.plottable()).isFalse();
    assertThat(evaluation.assessment()).isNull();
    assertThat(evaluation.point()).isEmpty();
    assertThat(evaluation.capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.ACIDITY);
  }

  @Test
  void exposesDirectChemicalProfileToSugarAcidityPointProjection() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.060")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("4.50")))
        .build();

    assertThat(projector.projectPoint(profile))
        .hasValueSatisfying(point -> assertThat(point.acidity().value()).isEqualByComparingTo("4.50"));
  }
}
