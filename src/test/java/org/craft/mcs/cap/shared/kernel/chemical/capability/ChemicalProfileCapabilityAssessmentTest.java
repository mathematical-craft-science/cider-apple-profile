package org.craft.mcs.cap.shared.kernel.chemical.capability;

import java.util.EnumSet;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ChemicalProfileCapabilityAssessmentTest {

  @Test
  void copiesAxisSets() {
    final var requiredAxes = EnumSet.of(ChemicalAxis.SUGAR, ChemicalAxis.ACIDITY);
    final var presentAxes = EnumSet.of(ChemicalAxis.SUGAR);
    final var missingAxes = EnumSet.of(ChemicalAxis.ACIDITY);

    final var assessment = new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT,
        false,
        requiredAxes,
        presentAxes,
        missingAxes
    );
    requiredAxes.clear();
    presentAxes.clear();
    missingAxes.clear();

    assertThat(assessment.requiredAxes()).containsExactlyInAnyOrder(ChemicalAxis.SUGAR, ChemicalAxis.ACIDITY);
    assertThat(assessment.presentAxes()).containsExactly(ChemicalAxis.SUGAR);
    assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.ACIDITY);
  }

  @Test
  void exposesImmutableAxisSets() {
    final var assessment = new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT,
        false,
        Set.of(ChemicalAxis.SUGAR, ChemicalAxis.ACIDITY),
        Set.of(ChemicalAxis.SUGAR),
        Set.of(ChemicalAxis.ACIDITY)
    );

    assertThatThrownBy(() -> assessment.missingAxes().add(ChemicalAxis.TANNINITY))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void rejectsNullCapability() {
    assertThatNullPointerException()
        .isThrownBy(() -> new ChemicalProfileCapabilityAssessment(
            null,
            false,
            Set.of(ChemicalAxis.SUGAR),
            Set.of(),
            Set.of(ChemicalAxis.SUGAR)
        ));
  }
}
