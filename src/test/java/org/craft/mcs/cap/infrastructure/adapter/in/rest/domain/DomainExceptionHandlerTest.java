package org.craft.mcs.cap.infrastructure.adapter.in.rest.domain;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.InvalidChemicalProfileCompositionException;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.UnsupportedCompositionOptimizationStrategyException;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolutionException;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.sugar.converter.UnsupportedSugarConversionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;


class DomainExceptionHandlerTest {

  @Test
  void mapsDomainRuleViolationToProblemDetail() {
    final var response = new DomainExceptionHandler().handleDomainRuleViolation(
        new DomainRuleViolationException("invalid domain state")
    );

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Domain rule violation");
    assertThat(response.getBody().getDetail()).isEqualTo("invalid domain state");
  }

  @Test
  void mapsDomainConflictToProblemDetail() {
    final var response = new DomainExceptionHandler().handleDomainConflict(
        new DomainConflictException("volume changed")
    );

    assertThat(response.getStatusCode().value()).isEqualTo(409);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Domain state conflict");
    assertThat(response.getBody().getDetail()).isEqualTo("volume changed");
  }

  @Test
  void mapsUnsupportedSugarConversionToBadRequest() {
    final var response = new DomainExceptionHandler().handleUnsupportedSugarConversion(
        new UnsupportedSugarConversionException(String.class, Integer.class)
    );

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Unsupported sugar conversion");
  }

  @Test
  void mapsInvalidChemicalProfileCompositionToUnprocessableEntity() {
    final var response = new DomainExceptionHandler().handleInvalidChemicalProfileComposition(
        new InvalidChemicalProfileCompositionException("composition is invalid")
    );

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Invalid chemical profile composition");
    assertThat(response.getBody().getDetail()).isEqualTo("composition is invalid");
  }

  @Test
  void mapsUnsupportedCompositionStrategyToBadRequest() {
    final var response = new DomainExceptionHandler().handleUnsupportedCompositionStrategy(
        new UnsupportedCompositionOptimizationStrategyException(CompositionOptimizationAlgorithm.LINEAR_PROGRAMMING)
    );

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Unsupported composition strategy");
  }

  @Test
  void mapsUnresolvedSugarAcidityTargetToUnprocessableEntity() {
    final var response = new DomainExceptionHandler().handleSugarAcidityTargetResolution(
        new SugarAcidityTargetResolutionException("target is unknown")
    );

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getBody())
        .isNotNull()
        .returns("Sugar-acidity target cannot be resolved", ProblemDetail::getTitle)
        .returns("target is unknown", ProblemDetail::getDetail);
  }
}
