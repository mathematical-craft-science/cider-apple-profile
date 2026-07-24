package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.prediction;


class AppleVarietySugarAcidityZoneRestMapperTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void serializesAxisPointDiscriminatorForPlottableSingleResult() throws Exception {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.066")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.7")))
        .build();
    final var axes = new ChemicalProfileAxisExtractor().extract(profile);
    final var assessment = new RectangleSugarAcidityZoneAssessor(
        BuiltInSugarAcidityZoneProfiles.thresholds(),
        BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
    ).assess(axes).orElseThrow();
    final var capabilityAssessment = new ChemicalProfileCapabilityAssessor().assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    final var response = AppleVarietySugarAcidityZoneRestMapper.toResponse(
        new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyName.of("Kingston Black"),
            new SugarAcidityProfileEvaluation(axes, capabilityAssessment, true, assessment),
            prediction(SpecificGravityValue.of("1.066"))
        )
    );

    final var json = OBJECT_MAPPER.valueToTree(response);

    assertThat(json.at("/assessment/point/sugar/valueKind").asText()).isEqualTo("POINT");
    assertThat(json.at("/assessment/point/acidity/valueKind").asText()).isEqualTo("POINT");
    assertThat(json.at("/potentialAbv/potentialAbv/value").decimalValue()).isEqualByComparingTo("8.663");
  }

  @Test
  void mapsUnplottableSingleResultWithCapabilityAssessment() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();
    final var axes = new ChemicalProfileAxisExtractor().extract(profile);
    final var capabilityAssessment = new ChemicalProfileCapabilityAssessor().assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    final var response = AppleVarietySugarAcidityZoneRestMapper.toResponse(
        new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyName.of("Partial Profile"),
            new SugarAcidityProfileEvaluation(axes, capabilityAssessment, false, null),
            prediction(SpecificGravityValue.of("1.050"))
        )
    );

    assertThat(response.varietyId()).hasToString("00000000-0000-0000-0000-000000000001");
    assertThat(response.varietyName()).isEqualTo("Partial Profile");
    assertThat(response.plottable()).isFalse();
    assertThat(response.assessment()).isNull();
    assertThat(response.capabilityAssessment()).isNotNull();
    assertThat(response.capabilityAssessment().missingAxes()).containsExactly(ChemicalAxis.ACIDITY);
    assertThat(response.potentialAbv()).isNotNull();
  }
}
