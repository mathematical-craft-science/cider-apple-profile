package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis;

import org.craft.mcs.cap.core.cideranalysis.targetfit.application.TargetFitTargetUnavailableException;
import org.craft.mcs.cap.core.formulation.calculation.application.InvalidCompositionRequestException;
import org.craft.mcs.cap.core.formulation.optimization.application.UnsupportedCompositionOptimizationRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;


class AnalysisApplicationExceptionHandlerTest {

  private static final RuntimeException CAUSE = new RuntimeException("internal analysis failure");

  @Test
  void mapsInvalidCompositionToUnprocessableEntity() {
    final var response = new AnalysisApplicationExceptionHandler().handleInvalidComposition(
        new InvalidCompositionRequestException("composition is invalid", CAUSE)
    );

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getBody())
        .isNotNull()
        .returns("Invalid composition", ProblemDetail::getTitle)
        .returns("composition is invalid", ProblemDetail::getDetail);
  }

  @Test
  void mapsUnsupportedCompositionStrategyToBadRequest() {
    final var response = new AnalysisApplicationExceptionHandler().handleUnsupportedCompositionStrategy(
        new UnsupportedCompositionOptimizationRequestException("strategy is unsupported", CAUSE)
    );

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody())
        .isNotNull()
        .returns("Unsupported composition strategy", ProblemDetail::getTitle)
        .returns("strategy is unsupported", ProblemDetail::getDetail);
  }

  @Test
  void mapsUnresolvedSugarAcidityTargetToUnprocessableEntity() {
    final var response = new AnalysisApplicationExceptionHandler().handleTargetUnavailable(
        new TargetFitTargetUnavailableException("target is unknown", CAUSE)
    );

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getBody())
        .isNotNull()
        .returns("Sugar-acidity target cannot be resolved", ProblemDetail::getTitle)
        .returns("target is unknown", ProblemDetail::getDetail);
  }
}
