package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.capability.mapper;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileAnalysisCapabilityRestMapperTest {

  @Test
  void mapsCharacteristicsInStableEnumOrder() {
    final var response = ProfileAnalysisCapabilityRestMapper.toResponse(
        JolicoeurSugarAcidityGraphEvaluation.unplottable(
            Set.of(GraphMissingCharacteristic.TITRATABLE_ACIDITY)
        )
    );

    assertThat(response.capability()).isEqualTo(ProfileAnalysisCapability.SUGAR_ACIDITY_GRAPH);
    assertThat(response.capable()).isFalse();
    assertThat(response.requiredCharacteristics()).containsExactly(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementCharacteristic.TITRATABLE_ACIDITY
    );
    assertThat(response.presentCharacteristics()).containsExactly(
        MeasurementCharacteristic.SPECIFIC_GRAVITY
    );
    assertThat(response.missingCharacteristics()).containsExactly(
        MeasurementCharacteristic.TITRATABLE_ACIDITY
    );
  }
}
