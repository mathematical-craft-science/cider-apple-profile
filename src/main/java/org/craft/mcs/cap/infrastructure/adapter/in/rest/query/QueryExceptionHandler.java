package org.craft.mcs.cap.infrastructure.adapter.in.rest.query;


import org.craft.mcs.cap.shared.platform.query.MalformedCursorException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;


@RestControllerAdvice
public final class QueryExceptionHandler {

  private static final @NonNull URI MALFORMED_CURSOR =
      URI.create("urn:problem-type:malformed-cursor");
  private static final int BAD_REQUEST = 400;

  @ExceptionHandler(MalformedCursorException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleMalformedCursor(
      @NonNull MalformedCursorException exception
  ) {
    final var problem = ProblemDetail.forStatus(BAD_REQUEST);

    problem.setType(MALFORMED_CURSOR);
    problem.setTitle("Malformed cursor");
    problem.setDetail("The pagination cursor is malformed or no longer valid for this query.");
    problem.setProperty("parameter", "after");

    return ResponseEntity
        .status(BAD_REQUEST)
        .body(problem);
  }
}
