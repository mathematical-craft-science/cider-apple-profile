package org.craft.mcs.cap.infrastructure.adapter.in.rest.application;

import java.net.URI;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.create;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


/**
 * Fallback REST mapper for application-layer failures that are not covered by a bounded-context
 * specific exception handler.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public final class ApplicationExceptionHandler {

  private static final @NonNull URI APPLICATION_RESOURCE_NOT_FOUND =
      URI.create("urn:problem-type:application-resource-not-found");
  private static final @NonNull URI APPLICATION_FORBIDDEN =
      URI.create("urn:problem-type:application-forbidden");
  private static final @NonNull URI APPLICATION_CONFLICT =
      URI.create("urn:problem-type:application-conflict");
  private static final @NonNull URI CONCURRENT_RESOURCE_MODIFICATION =
      URI.create("urn:problem-type:concurrent-resource-modification");
  private static final @NonNull URI APPLICATION_UNPROCESSABLE =
      URI.create("urn:problem-type:application-unprocessable");
  private static final @NonNull URI APPLICATION_VALIDATION_FAILED =
      URI.create("urn:problem-type:application-validation-failed");
  private static final int BAD_REQUEST = 400;
  private static final int NOT_FOUND = 404;
  private static final int FORBIDDEN = 403;
  private static final int CONFLICT = 409;
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(ApplicationNotFoundException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleNotFound(
      @NonNull ApplicationNotFoundException exception
  ) {
    return response(
        NOT_FOUND,
        APPLICATION_RESOURCE_NOT_FOUND,
        "Application resource not found",
        exception.getMessage()
    );
  }

  @ExceptionHandler(ApplicationForbiddenException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleForbidden(
      @NonNull ApplicationForbiddenException exception
  ) {
    return response(
        FORBIDDEN,
        APPLICATION_FORBIDDEN,
        "Application action is forbidden",
        exception.getMessage()
    );
  }

  @ExceptionHandler(ConcurrentResourceModificationException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleConcurrentResourceModification(
      @NonNull ConcurrentResourceModificationException exception
  ) {
    final var problem = create(
        CONFLICT,
        CONCURRENT_RESOURCE_MODIFICATION,
        "Resource was modified concurrently",
        "The resource was changed by another request. Reload it and retry the operation."
    );
    problem.setProperty("resource", exception.resourceName());

    return response(CONFLICT, problem);
  }

  @ExceptionHandler(ApplicationConflictException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleConflict(
      @NonNull ApplicationConflictException exception
  ) {
    return response(
        CONFLICT,
        APPLICATION_CONFLICT,
        "Application state conflict",
        exception.getMessage()
    );
  }

  @ExceptionHandler(ApplicationUnprocessableException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleUnprocessable(
      @NonNull ApplicationUnprocessableException exception
  ) {
    return response(
        UNPROCESSABLE_ENTITY,
        APPLICATION_UNPROCESSABLE,
        "Application request cannot be processed",
        exception.getMessage()
    );
  }

  @ExceptionHandler(ApplicationValidationException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleValidation(
      @NonNull ApplicationValidationException exception
  ) {
    return response(
        BAD_REQUEST,
        APPLICATION_VALIDATION_FAILED,
        "Application request validation failed",
        exception.getMessage()
    );
  }

  @ExceptionHandler(ApplicationException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleApplicationException(
      @NonNull ApplicationException exception
  ) {
    return response(
        BAD_REQUEST,
        APPLICATION_VALIDATION_FAILED,
        "Application error",
        exception.getMessage()
    );
  }
}
