package org.craft.mcs.cap.infrastructure.adapter.in.rest.domain;

import java.net.URI;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.InvalidCompositionException;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.UnsupportedCompositionCalculatorException;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.InvalidCompositionOptimizationException;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.UnsupportedCompositionOptimizationStrategyException;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolutionException;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.sugar.converter.UnsupportedSugarConversionException;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public final class DomainExceptionHandler {

  private static final @NonNull URI DOMAIN_RULE_VIOLATION =
      URI.create("urn:problem-type:domain-rule-violation");
  private static final @NonNull URI DOMAIN_CONFLICT =
      URI.create("urn:problem-type:domain-conflict");
  private static final @NonNull URI UNSUPPORTED_SUGAR_CONVERSION =
      URI.create("urn:problem-type:unsupported-sugar-conversion");
  private static final @NonNull URI INVALID_COMPOSITION =
      URI.create("urn:problem-type:invalid-composition");
  private static final @NonNull URI UNSUPPORTED_COMPOSITION_STRATEGY =
      URI.create("urn:problem-type:unsupported-composition-strategy");
  private static final @NonNull URI SUGAR_ACIDITY_TARGET_NOT_RESOLVED =
      URI.create("urn:problem-type:sugar-acidity-target-not-resolved");
  private static final int BAD_REQUEST = 400;
  private static final int CONFLICT = 409;
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(UnsupportedSugarConversionException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleUnsupportedSugarConversion(
      @NonNull UnsupportedSugarConversionException exception
  ) {
    return response(
        BAD_REQUEST,
        UNSUPPORTED_SUGAR_CONVERSION,
        "Unsupported sugar conversion",
        exception.getMessage()
    );
  }

  @ExceptionHandler(DomainConflictException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleDomainConflict(
      @NonNull DomainConflictException exception
  ) {
    return response(
        CONFLICT,
        DOMAIN_CONFLICT,
        "Domain state conflict",
        exception.getMessage()
    );
  }

  @ExceptionHandler({
      InvalidCompositionException.class,
      InvalidCompositionOptimizationException.class
  })
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleInvalidComposition(
      @NonNull DomainException exception
  ) {
    return response(
        UNPROCESSABLE_ENTITY,
        INVALID_COMPOSITION,
        "Invalid composition",
        exception.getMessage()
    );
  }

  @ExceptionHandler({
      UnsupportedCompositionCalculatorException.class,
      UnsupportedCompositionOptimizationStrategyException.class
  })
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleUnsupportedCompositionStrategy(
      @NonNull DomainException exception
  ) {
    return response(
        BAD_REQUEST,
        UNSUPPORTED_COMPOSITION_STRATEGY,
        "Unsupported composition strategy",
        exception.getMessage()
    );
  }

  @ExceptionHandler(SugarAcidityTargetResolutionException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleSugarAcidityTargetResolution(
      @NonNull SugarAcidityTargetResolutionException exception
  ) {
    return response(
        UNPROCESSABLE_ENTITY,
        SUGAR_ACIDITY_TARGET_NOT_RESOLVED,
        "Sugar-acidity target cannot be resolved",
        exception.getMessage()
    );
  }

  @ExceptionHandler(DomainRuleViolationException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleDomainRuleViolation(
      @NonNull DomainRuleViolationException exception
  ) {
    return response(
        UNPROCESSABLE_ENTITY,
        DOMAIN_RULE_VIOLATION,
        "Domain rule violation",
        exception.getMessage()
    );
  }

  @ExceptionHandler(DomainException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleDomainException(
      @NonNull DomainException exception
  ) {
    return response(
        BAD_REQUEST,
        DOMAIN_RULE_VIOLATION,
        "Domain error",
        exception.getMessage()
    );
  }
}
