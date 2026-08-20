package org.craft.mcs.cap.infrastructure.adapter.in.rest.domain;

import java.net.URI;
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
