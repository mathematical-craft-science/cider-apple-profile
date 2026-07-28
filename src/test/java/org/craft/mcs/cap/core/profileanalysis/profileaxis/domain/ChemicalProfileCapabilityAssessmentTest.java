package org.craft.mcs.cap.core.profileanalysis.profileaxis.domain;

import java.util.EnumSet;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ChemicalProfileCapabilityAssessmentTest {

  @Test
  void copiesAxisSets() {
    final var requiredAxes = EnumSet.of(ChemicalAxis.SPECIFIC_GRAVITY, ChemicalAxis.TITRATABLE_ACIDITY);
    final var presentAxes = EnumSet.of(ChemicalAxis.SPECIFIC_GRAVITY);
    final var missingAxes = EnumSet.of(ChemicalAxis.TITRATABLE_ACIDITY);

    final var assessment = new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH,
        requiredAxes,
        presentAxes
    );
    requiredAxes.clear();
    presentAxes.clear();
    missingAxes.clear();

    assertThat(assessment.requiredAxes())
        .containsExactlyInAnyOrder(
            ChemicalAxis.SPECIFIC_GRAVITY,
            ChemicalAxis.TITRATABLE_ACIDITY
        );
    assertThat(assessment.presentAxes()).containsExactly(ChemicalAxis.SPECIFIC_GRAVITY);
    assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.TITRATABLE_ACIDITY);
  }

  @Test
  void exposesImmutableAxisSets() {
    final var assessment = new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH,
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY, ChemicalAxis.TITRATABLE_ACIDITY),
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY)
    );

    assertThatThrownBy(() -> assessment.missingAxes().add(ChemicalAxis.TANNIN))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void rejectsNullCapability() {
    assertThatNullPointerException()
        .isThrownBy(() -> new ChemicalProfileCapabilityAssessment(
            null,
            Set.of(ChemicalAxis.SPECIFIC_GRAVITY),
            Set.of()
        ));
  }

  @Test
  void derivesCapabilityAndMissingAxesFromRequiredAndPresentAxes() {
    final var assessment = new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH,
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY, ChemicalAxis.TITRATABLE_ACIDITY),
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY)
    );

    assertThat(assessment)
        .returns(false, ChemicalProfileCapabilityAssessment::capable)
        .satisfies(candidate -> assertThat(candidate.missingAxes())
            .containsExactly(ChemicalAxis.TITRATABLE_ACIDITY));
  }

  @Test
  void rejectsPresentAxesOutsideCapabilityRequirements() {
    assertThatThrownBy(() -> new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.ABV_PREDICTION,
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY),
        Set.of(ChemicalAxis.SPECIFIC_GRAVITY, ChemicalAxis.TANNIN)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("present chemical axes must be required by the capability");
  }

  @Test
  void rejectsCapabilityWithoutRequiredAxes() {
    assertThatThrownBy(() -> new ChemicalProfileCapabilityAssessment(
        ChemicalProfileCapability.ABV_PREDICTION,
        Set.of(),
        Set.of()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("chemical profile capability must require at least one axis");
  }
}
