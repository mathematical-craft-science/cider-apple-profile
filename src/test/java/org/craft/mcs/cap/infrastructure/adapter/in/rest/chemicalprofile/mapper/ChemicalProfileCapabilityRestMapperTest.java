package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.util.Set;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileCapabilityRestMapperTest {

  @Test
  void mapsAxesInStableEnumOrder() {
    final var response = ChemicalProfileCapabilityRestMapper.toResponse(
        new ChemicalProfileCapabilityAssessment(
            ChemicalProfileCapability.SUGAR_ACIDITY_PLOT,
            false,
            Set.of(ChemicalAxis.ACIDITY, ChemicalAxis.SUGAR),
            Set.of(ChemicalAxis.SUGAR),
            Set.of(ChemicalAxis.ACIDITY)
        )
    );

    assertThat(response.capability()).isEqualTo("SUGAR_ACIDITY_PLOT");
    assertThat(response.capable()).isFalse();
    assertThat(response.requiredAxes()).containsExactly("SUGAR", "ACIDITY");
    assertThat(response.presentAxes()).containsExactly("SUGAR");
    assertThat(response.missingAxes()).containsExactly("ACIDITY");
  }
}
