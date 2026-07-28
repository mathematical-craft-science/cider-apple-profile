package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.prediction;


class AppleVarietyProfileGraphRestMapperTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void serializesAxisPointDiscriminatorForPlottableSingleResult() throws Exception {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.066")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.7")))
        .tannin(PointCharacteristicValue.of(TanninValue.of("2000")))
        .build();
    final var evaluation = chemicalProfileAssessmentAdapter().project(profile);

    final var response = AppleVarietyProfileGraphRestMapper.toResponse(
        new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyName.of("Kingston Black"),
            evaluation,
            prediction(SpecificGravityValue.of("1.066"))
        )
    );

    final var json = OBJECT_MAPPER.valueToTree(response);

    assertThat(json.at("/assessment/point/specificGravity/valueKind").asText()).isEqualTo("POINT");
    assertThat(json.at("/assessment/point/titratableAcidity/valueKind").asText()).isEqualTo("POINT");
    assertThat(json.at("/assessment").has("zoneSystem")).isFalse();
    assertThat(json.at("/assessment/northAmericanTanninLevel").asText()).isEqualTo("MEDIUM");
    assertThat(json.at("/assessment").has("balancedProfile")).isFalse();
    assertThat(json.at("/potentialAbv/potentialAbv/value").decimalValue()).isEqualByComparingTo("8.663");
  }

  @Test
  void preservesIntervalShapeInCompatibilityResponse() {
    final var profile = ChemicalProfile.builder()
        .sugar(IntervalCharacteristicValue.of(
            SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.060")),
            SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.070"))
        ))
        .titratableAcidity(IntervalCharacteristicValue.of(
            TitratableAcidityValue.of("5.0"),
            TitratableAcidityValue.of("6.0")
        ))
        .build();

    final var response = AppleVarietyProfileGraphRestMapper.toResponse(
        new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyName.of("Interval Profile"),
            chemicalProfileAssessmentAdapter().project(profile),
            prediction(SpecificGravityValue.of("1.065"))
        )
    );
    final var json = OBJECT_MAPPER.valueToTree(response);

    assertThat(json.at("/assessment/point/specificGravity/valueKind").asText())
        .isEqualTo("INTERVAL_MIDPOINT");
    assertThat(json.at("/assessment/point/titratableAcidity/valueKind").asText())
        .isEqualTo("INTERVAL_MIDPOINT");
  }

  @Test
  void mapsUnplottableSingleResultWithCapabilityAssessment() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();

    final var response = AppleVarietyProfileGraphRestMapper.toResponse(
        new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyName.of("Partial Profile"),
            chemicalProfileAssessmentAdapter().project(profile),
            prediction(SpecificGravityValue.of("1.050"))
        )
    );

    assertThat(response.varietyId()).hasToString("00000000-0000-0000-0000-000000000001");
    assertThat(response.varietyName()).isEqualTo("Partial Profile");
    assertThat(response.plottable()).isFalse();
    assertThat(response.assessment()).isNull();
    assertThat(response.capabilityAssessment()).isNotNull();
    assertThat(response.capabilityAssessment().missingAxes()).containsExactly(ChemicalAxis.TITRATABLE_ACIDITY);
    assertThat(response.potentialAbv()).isNotNull();
  }
}
