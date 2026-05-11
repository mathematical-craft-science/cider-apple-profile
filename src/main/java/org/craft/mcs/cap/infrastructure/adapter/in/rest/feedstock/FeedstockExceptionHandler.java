package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;


import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;


@RestControllerAdvice
public final class FeedstockExceptionHandler {

  private static final @NonNull URI FEEDSTOCK_BATCH_NOT_FOUND =
      URI.create("urn:problem-type:feedstock-batch-not-found");
  private static final int NOT_FOUND = 404;

  @ExceptionHandler(FeedstockBatchNotFoundException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleFeedstockBatchNotFound(
      @NonNull FeedstockBatchNotFoundException exception
  ) {
    final var problem = ProblemDetail.forStatus(NOT_FOUND);

    problem.setType(FEEDSTOCK_BATCH_NOT_FOUND);
    problem.setTitle("Feedstock batch not found");
    problem.setDetail("Feedstock batch does not exist: " + exception.batchId().value());
    problem.setProperty("batchId", exception.batchId().value().toString());

    return ResponseEntity
        .status(NOT_FOUND)
        .body(problem);
  }
}
