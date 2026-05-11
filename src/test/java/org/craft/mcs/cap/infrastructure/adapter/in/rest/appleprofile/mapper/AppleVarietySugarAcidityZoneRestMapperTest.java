package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper;

import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AppleVarietySugarAcidityZoneRestMapperTest {

  @Test
  void mapsUnplottableSingleResultWithCapabilityAssessment() {
    final var profile = ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();
    final var capabilityAssessment = new ChemicalProfileCapabilityAssessor().assess(
        new ChemicalProfileAxisExtractor().extract(profile),
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    final var response = AppleVarietySugarAcidityZoneRestMapper.toResponse(
        new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.of("00000000-0000-0000-0000-000000000001"),
            AppleVarietyName.of("Partial Profile"),
            false,
            null,
            capabilityAssessment
        )
    );

    assertThat(response.varietyId()).hasToString("00000000-0000-0000-0000-000000000001");
    assertThat(response.varietyName()).isEqualTo("Partial Profile");
    assertThat(response.plottable()).isFalse();
    assertThat(response.assessment()).isNull();
    assertThat(response.capabilityAssessment()).isNotNull();
    assertThat(response.capabilityAssessment().missingAxes()).containsExactly(ChemicalAxis.ACIDITY);
  }
}
