package org.craft.mcs.cap.core.profileanalysis.compatibility.application;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ChemicalProfileAssessmentTest {

  @Test
  void shouldRejectCapabilityEvidenceThatDoesNotMatchAxes() {
    final var axes = ChemicalProfileAxes.of(null, null, null);
    final var graphEvaluation = JolicoeurSugarAcidityGraphEvaluation.unplottable(
        Set.of(GraphMissingCharacteristic.TITRATABLE_ACIDITY)
    );
    final var capabilityAssessment = new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH,
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY, ChemicalAxis.TITRATABLE_ACIDITY),
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY)
    );

    assertThatThrownBy(() -> new ChemicalProfileAssessment(
        axes,
        graphEvaluation,
        null,
        capabilityAssessment
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("capability evidence")
        .hasMessageContaining("SPECIFIC_GRAVITY");
  }

  @Test
  void shouldRejectGraphEvidenceThatDoesNotMatchAxes() {
    final var axes = ChemicalProfileAxes.of(
        AxisValue.point(ChemicalAxis.SPECIFIC_GRAVITY, new BigDecimal("1.050")),
        null,
        null
    );
    final var graphEvaluation = JolicoeurSugarAcidityGraphEvaluation.unplottable(
        Set.of(
            GraphMissingCharacteristic.SPECIFIC_GRAVITY,
            GraphMissingCharacteristic.TITRATABLE_ACIDITY
        )
    );
    final var capabilityAssessment = new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH,
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY, ChemicalAxis.TITRATABLE_ACIDITY),
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY)
    );

    assertThatThrownBy(() -> new ChemicalProfileAssessment(
        axes,
        graphEvaluation,
        null,
        capabilityAssessment
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("graph evidence")
        .hasMessageContaining("SPECIFIC_GRAVITY");
  }
}
