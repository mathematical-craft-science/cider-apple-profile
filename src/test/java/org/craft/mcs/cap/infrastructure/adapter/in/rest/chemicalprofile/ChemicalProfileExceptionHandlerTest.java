package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile;


import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityNotMetException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileExceptionHandlerTest {

  @Test
  void mapsCapabilityExceptionToProblemDetail() {
    final var response = new ChemicalProfileExceptionHandler().handleChemicalProfileCapabilityNotMet(
        new ChemicalProfileCapabilityNotMetException(
            new ChemicalProfileCapabilityAssessment(
                ChemicalProfileCapability.NORTH_AMERICAN_PROFILE,
                false,
                Set.of(ChemicalAxis.SUGAR, ChemicalAxis.ACIDITY, ChemicalAxis.TANNIN),
                Set.of(ChemicalAxis.SUGAR),
                Set.of(ChemicalAxis.ACIDITY, ChemicalAxis.TANNIN)
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
        .containsEntry("requiredAxes", List.of("SUGAR", "ACIDITY", "TANNIN"))
        .containsEntry("presentAxes", List.of("SUGAR"))
        .containsEntry("missingAxes", List.of("ACIDITY", "TANNIN"));
  }
}
