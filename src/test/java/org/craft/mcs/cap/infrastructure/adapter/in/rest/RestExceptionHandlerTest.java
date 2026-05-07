package org.craft.mcs.cap.infrastructure.adapter.in.rest;

import java.util.Set;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

import static org.assertj.core.api.Assertions.assertThat;


class RestExceptionHandlerTest {

  @Test
  void mapsCapabilityExceptionToProblemDetail() {
    final var response = new RestExceptionHandler().handleChemicalProfileCapabilityNotMet(
        new ChemicalProfileCapabilityNotMetException(
            new ChemicalProfileCapabilityAssessment(
                ChemicalProfileCapability.NORTH_AMERICAN_PROFILE,
                false,
                Set.of(ChemicalAxis.SUGAR, ChemicalAxis.ACIDITY, ChemicalAxis.TANNINITY),
                Set.of(ChemicalAxis.SUGAR),
                Set.of(ChemicalAxis.ACIDITY, ChemicalAxis.TANNINITY)
            )
        )
    );

    final var problem = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(422));
    assertThat(problem).isNotNull();
    assertThat(problem.getTitle()).isEqualTo("Chemical profile does not meet capability requirements");
    assertThat(problem.getDetail()).contains("NORTH_AMERICAN_PROFILE");
    assertThat(problem.getProperties())
        .containsEntry("capability", "NORTH_AMERICAN_PROFILE")
        .containsEntry("requiredAxes", java.util.List.of("SUGAR", "ACIDITY", "TANNINITY"))
        .containsEntry("presentAxes", java.util.List.of("SUGAR"))
        .containsEntry("missingAxes", java.util.List.of("ACIDITY", "TANNINITY"));
  }
}
