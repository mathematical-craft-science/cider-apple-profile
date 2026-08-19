package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures.graphEvaluator;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.craft.mcs.cap.shared.testing.AppleVarietyReferenceProfiles.from;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZoneResponse;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.junit.jupiter.api.Test;


class AppleVarietyProfileGraphRestMapperTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void serializesAxisPointDiscriminatorForPlottableSingleResult()
      throws Exception {
    final var profile = ProfileCharacteristicsFixture.builder()
        .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.066")))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.7")))
        .tannin(PointCharacteristicValue.of(TanninValue.of("2000")))
        .build();
    final var response = AppleVarietyProfileGraphRestMapper.toResponse(
        result(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyAnalysisName.of("Kingston Black"),
            profile,
            prediction(SpecificGravityValue.of("1.066"))
        )
    );

    final var json = OBJECT_MAPPER.valueToTree(response);

    assertThatObject(json)
        .returns("POINT", node -> node.at("/assessment/point/specificGravity/valueKind").asText())
        .returns("POINT", node -> node.at("/assessment/point/titratableAcidity/valueKind").asText())
        .returns(false, node -> node.at("/assessment").has("zoneSystem"))
        .returns(true, node -> node.at("/assessment/northAmericanTanninLevel").isNull())
        .returns("HIGH", node -> node.at("/northAmericanProfile/sugar").asText())
        .returns("MEDIUM", node -> node.at("/northAmericanProfile/acidity").asText())
        .returns("MEDIUM", node -> node.at("/northAmericanProfile/tannin").asText())
        .returns(false, node -> node.at("/assessment").has("balancedProfile"))
        .satisfies(node -> assertThat(node.at("/tannin/value/value").decimalValue())
            .isEqualByComparingTo("2000"))
        .satisfies(node -> assertThat(node.at("/potentialAbv/potentialAbv/value").decimalValue())
            .isEqualByComparingTo("8.663"));
  }

  @Test
  void preservesIntervalShapeInCompatibilityResponse() {
    final var profile = ProfileCharacteristicsFixture.builder()
        .specificGravity(IntervalCharacteristicValue.of(
            SpecificGravityValue.of("1.060"),
            SpecificGravityValue.of("1.070")
        ))
        .titratableAcidity(IntervalCharacteristicValue.of(
            TitratableAcidityValue.of("5.0"),
            TitratableAcidityValue.of("6.0")
        ))
        .build();

    final var response = AppleVarietyProfileGraphRestMapper.toResponse(
        result(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyAnalysisName.of("Interval Profile"),
            profile,
            prediction(SpecificGravityValue.of("1.065"))
        )
    );
    final var json = OBJECT_MAPPER.valueToTree(response);

    assertThatObject(json)
        .returns(
            "INTERVAL_MIDPOINT",
            node -> node.at("/assessment/point/specificGravity/valueKind").asText()
        )
        .returns(
            "INTERVAL_MIDPOINT",
            node -> node.at("/assessment/point/titratableAcidity/valueKind").asText()
        );
  }

  @Test
  void mapsUnplottableSingleResultWithCapabilityAssessment() {
    final var profile = ProfileCharacteristicsFixture.builder()
        .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.050")))
        .build();

    final var response = AppleVarietyProfileGraphRestMapper.toResponse(
        result(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyAnalysisName.of("Partial Profile"),
            profile,
            prediction(SpecificGravityValue.of("1.050"))
        )
    );

    assertThat(response)
        .returns(
            "00000000-0000-0000-0000-000000000001",
            value -> value.varietyId().toString()
        )
        .returns("Partial Profile", AppleVarietySugarAcidityZoneResponse::varietyName)
        .returns(false, AppleVarietySugarAcidityZoneResponse::plottable)
        .returns(null, AppleVarietySugarAcidityZoneResponse::assessment)
        .matches(value -> value.potentialAbv() != null)
        .satisfies(value -> assertThat(value.missingCharacteristics())
            .containsExactly(GraphMissingCharacteristic.TITRATABLE_ACIDITY));
  }

  private static AppleVarietyGraphAnalysisView result(
      AppleVarietyId varietyId,
      AppleVarietyAnalysisName varietyName,
      ProfileCharacteristicsFixture profile,
      PotentialAbvPrediction potentialAbv
  ) {
    final var referenceProfile = from(profile);
    final var graphEvaluation = graphEvaluator().evaluate(
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity()
    );
    final var northAmericanAssessment = new AppleVarietyNorthAmericanAssessmentView(
        referenceProfile.specificGravity() == null
        ? null
        : northAmericanProfileCalculator().assessSugar(referenceProfile.specificGravity()),
        referenceProfile.titratableAcidity() == null
        ? null
        : northAmericanProfileCalculator().assessAcidity(referenceProfile.titratableAcidity()),
        referenceProfile.tannin() == null
        ? null
        : northAmericanProfileCalculator().assessTannin(referenceProfile.tannin())
    );
    return new AppleVarietyGraphAnalysisView(
        varietyId,
        varietyName,
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity(),
        referenceProfile.tannin(),
        graphEvaluation,
        northAmericanAssessment,
        potentialAbv
    );
  }
}
