package org.craft.mcs.cap.infrastructure.adapter.in.rest.query;


import java.net.URI;
import org.craft.mcs.cap.shared.platform.query.MalformedCursorException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.create;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


@RestControllerAdvice
public final class QueryExceptionHandler {

  private static final @NonNull URI MALFORMED_CURSOR =
      URI.create("urn:problem-type:malformed-cursor");
  private static final int BAD_REQUEST = 400;

  @ExceptionHandler(MalformedCursorException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleMalformedCursor(
      @NonNull MalformedCursorException exception
  ) {
    final var problem = create(
        BAD_REQUEST,
        MALFORMED_CURSOR,
        "Malformed cursor",
        "The pagination cursor is malformed or no longer valid for this query."
    );
    problem.setProperty("parameter", "after");

    return response(BAD_REQUEST, problem);
  }
}
