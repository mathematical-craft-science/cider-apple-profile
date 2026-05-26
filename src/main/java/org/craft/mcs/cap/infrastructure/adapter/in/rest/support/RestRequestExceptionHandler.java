package org.craft.mcs.cap.infrastructure.adapter.in.rest.support;

import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.create;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


/**
 * Handles transport-level request binding errors raised by Spring MVC before a controller method is
 * invoked.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class RestRequestExceptionHandler {

  private static final @NonNull URI INVALID_REQUEST_PARAMETER =
      URI.create("urn:problem-type:invalid-request-parameter");
  private static final int BAD_REQUEST = 400;

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleMethodArgumentTypeMismatch(
      @NonNull MethodArgumentTypeMismatchException exception
  ) {
    final var problem = create(
        BAD_REQUEST,
        INVALID_REQUEST_PARAMETER,
        "Invalid request parameter",
        "Request parameter '%s' has an invalid value.".formatted(exception.getName())
    );
    problem.setProperty("parameter", exception.getName());
    problem.setProperty("value", stringValue(exception.getValue()));
    problem.setProperty("expectedType", expectedType(exception));

    return response(BAD_REQUEST, problem);
  }

  private static @Nullable String stringValue(@Nullable Object value) {
    return value == null ? null : value.toString();
  }

  private static @Nullable String expectedType(@NonNull MethodArgumentTypeMismatchException exception) {
    final var requiredType = exception.getRequiredType();
    return requiredType == null ? null : requiredType.getSimpleName();
  }
}
