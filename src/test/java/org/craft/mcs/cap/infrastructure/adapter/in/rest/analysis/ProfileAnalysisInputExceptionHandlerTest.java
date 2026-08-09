package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis;

import java.net.URI;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.application.classification.AppleVarietyNorthAmericanProfileUnavailableException;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileAnalysisInputExceptionHandlerTest {

  @Test
  void preservesExplainableAnalysisInputProblemContract() {
    final var response = new ProfileAnalysisInputExceptionHandler().handleInputUnavailable(
        new AppleVarietyNorthAmericanProfileUnavailableException(
            AppleVarietyId.newId(),
            Set.of(MeasurementCharacteristic.SPECIFIC_GRAVITY)
        )
    );
    final var problem = response.getBody();

    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatusCode.valueOf(422));
    assertThat(problem)
        .isNotNull()
        .returns(
            URI.create("urn:problem-type:profile-analysis-input-unavailable"),
            ProblemDetail::getType
        )
        .returns(
            "Profile analysis inputs are unavailable",
            ProblemDetail::getTitle
        )
        .returns(
            "Available characteristics do not satisfy NORTH_AMERICAN_CLASSIFICATION",
            ProblemDetail::getDetail
        )
        .satisfies(item -> assertThat(item.getProperties())
            .containsEntry("analysis", "NORTH_AMERICAN_CLASSIFICATION")
            .containsEntry(
                "requiredCharacteristics",
                List.of("SPECIFIC_GRAVITY", "TITRATABLE_ACIDITY", "TANNIN")
            )
            .containsEntry("presentCharacteristics", List.of("SPECIFIC_GRAVITY"))
            .containsEntry(
                "missingCharacteristics",
                List.of("TITRATABLE_ACIDITY", "TANNIN")
            ));
  }
}
