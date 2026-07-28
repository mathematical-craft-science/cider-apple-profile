package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileCapabilityRestMapperTest {

  @Test
  void mapsAxesInStableEnumOrder() {
    final var response = ChemicalProfileCapabilityRestMapper.toResponse(
        new ChemicalProfileCapabilityAssessment(
            ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH,
            Set.of(ChemicalAxis.TITRATABLE_ACIDITY, ChemicalAxis.SPECIFIC_GRAVITY),
            Set.of(ChemicalAxis.SPECIFIC_GRAVITY)
        )
    );

    assertThat(response.capability()).isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH);
    assertThat(response.capable()).isFalse();
    assertThat(response.requiredAxes()).containsExactly(ChemicalAxis.SPECIFIC_GRAVITY, ChemicalAxis.TITRATABLE_ACIDITY);
    assertThat(response.presentAxes()).containsExactly(ChemicalAxis.SPECIFIC_GRAVITY);
    assertThat(response.missingAxes()).containsExactly(ChemicalAxis.TITRATABLE_ACIDITY);
  }
}
