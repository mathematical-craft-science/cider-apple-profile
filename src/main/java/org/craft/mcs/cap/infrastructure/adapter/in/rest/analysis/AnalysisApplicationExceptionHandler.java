package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis;

import java.net.URI;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.TargetFitTargetUnavailableException;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.AppleTargetFitUnavailableException;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.FeedstockTargetFitUnavailableException;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.InvalidFeedstockCompositionOptimizationException;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.UnsupportedFeedstockCompositionOptimizationException;
import org.craft.mcs.cap.core.formulation.calculation.application.FormulationTargetFitUnavailableException;
import org.craft.mcs.cap.core.formulation.calculation.application.InvalidCompositionRequestException;
import org.craft.mcs.cap.core.formulation.calculation.application.UnsupportedCompositionCalculatorRequestException;
import org.craft.mcs.cap.core.formulation.optimization.application.InvalidCompositionOptimizationRequestException;
import org.craft.mcs.cap.core.formulation.optimization.application.UnsupportedCompositionOptimizationRequestException;
import org.craft.mcs.cap.core.production.planning.application.InvalidProductionCandidateCalculationException;
import org.craft.mcs.cap.core.production.planning.application.ProductionCandidateTargetFitUnavailableException;
import org.craft.mcs.cap.core.production.planning.application.UnsupportedProductionCandidateCalculationException;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


/** Preserves the public HTTP semantics of CiderAnalysis and Formulation application failures. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class AnalysisApplicationExceptionHandler {

  private static final @NonNull URI INVALID_COMPOSITION =
      URI.create("urn:problem-type:invalid-composition");
  private static final @NonNull URI UNSUPPORTED_COMPOSITION_STRATEGY =
      URI.create("urn:problem-type:unsupported-composition-strategy");
  private static final @NonNull URI SUGAR_ACIDITY_TARGET_NOT_RESOLVED =
      URI.create("urn:problem-type:sugar-acidity-target-not-resolved");
  private static final int BAD_REQUEST = 400;
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler({
      InvalidCompositionRequestException.class,
      InvalidCompositionOptimizationRequestException.class,
      InvalidFeedstockCompositionOptimizationException.class,
      InvalidProductionCandidateCalculationException.class
  })
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleInvalidComposition(
      @NonNull ApplicationException exception
  ) {
    return response(
        UNPROCESSABLE_ENTITY,
        INVALID_COMPOSITION,
        "Invalid composition",
        exception.getMessage()
    );
  }

  @ExceptionHandler({
      UnsupportedCompositionCalculatorRequestException.class,
      UnsupportedCompositionOptimizationRequestException.class,
      UnsupportedFeedstockCompositionOptimizationException.class,
      UnsupportedProductionCandidateCalculationException.class
  })
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleUnsupportedCompositionStrategy(
      @NonNull ApplicationException exception
  ) {
    return response(
        BAD_REQUEST,
        UNSUPPORTED_COMPOSITION_STRATEGY,
        "Unsupported composition strategy",
        exception.getMessage()
    );
  }

  @ExceptionHandler({
      TargetFitTargetUnavailableException.class,
      AppleTargetFitUnavailableException.class,
      FeedstockTargetFitUnavailableException.class,
      FormulationTargetFitUnavailableException.class,
      ProductionCandidateTargetFitUnavailableException.class
  })
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleTargetUnavailable(
      @NonNull ApplicationException exception
  ) {
    return response(
        UNPROCESSABLE_ENTITY,
        SUGAR_ACIDITY_TARGET_NOT_RESOLVED,
        "Sugar-acidity target cannot be resolved",
        exception.getMessage()
    );
  }
}
