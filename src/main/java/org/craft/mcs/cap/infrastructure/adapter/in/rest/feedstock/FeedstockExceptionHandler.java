package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;


import java.net.URI;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockMeasurementNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.InsufficientFeedstockCompositionSourcesException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice(assignableTypes = {
    FeedstockBatchController.class,
    FeedstockBatchCurrentAssessmentController.class,
    FeedstockMeasurementController.class,
    FeedstockMeasurementAssessmentController.class
})
public final class FeedstockExceptionHandler {

  private static final @NonNull URI INVALID_FEEDSTOCK_REQUEST =
      URI.create("urn:problem-type:invalid-feedstock-request");
  private static final @NonNull URI FEEDSTOCK_BATCH_NOT_FOUND =
      URI.create("urn:problem-type:feedstock-batch-not-found");
  private static final @NonNull URI FEEDSTOCK_BATCH_HAS_NO_MEASUREMENTS =
      URI.create("urn:problem-type:feedstock-batch-has-no-measurements");
  private static final @NonNull URI FEEDSTOCK_BATCH_OWNERSHIP_MISMATCH =
      URI.create("urn:problem-type:feedstock-batch-ownership-mismatch");
  private static final @NonNull URI FEEDSTOCK_MEASUREMENT_NOT_FOUND =
      URI.create("urn:problem-type:feedstock-measurement-not-found");
  private static final @NonNull URI INSUFFICIENT_FEEDSTOCK_COMPOSITION_SOURCES =
      URI.create("urn:problem-type:insufficient-feedstock-composition-sources");
  private static final int BAD_REQUEST = 400;
  private static final int NOT_FOUND = 404;
  private static final int FORBIDDEN = 403;
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(IllegalArgumentException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleIllegalArgument(
      @NonNull IllegalArgumentException exception
  ) {
    final var problem = ProblemDetail.forStatus(BAD_REQUEST);

    problem.setType(INVALID_FEEDSTOCK_REQUEST);
    problem.setTitle("Invalid feedstock request");
    problem.setDetail(exception.getMessage());

    return ResponseEntity
        .status(BAD_REQUEST)
        .body(problem);
  }

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

  @ExceptionHandler(FeedstockBatchOwnershipMismatchException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleFeedstockBatchOwnershipMismatch(
      @NonNull FeedstockBatchOwnershipMismatchException exception
  ) {
    final var problem = ProblemDetail.forStatus(FORBIDDEN);

    problem.setType(FEEDSTOCK_BATCH_OWNERSHIP_MISMATCH);
    problem.setTitle("Feedstock batch belongs to another cider maker");
    problem.setDetail("Feedstock batch cannot be modified by this cider maker.");
    problem.setProperty("batchId", exception.batchId().value().toString());
    problem.setProperty("expectedCiderMakerId", exception.expectedCiderMakerId().value().toString());
    problem.setProperty("actualCiderMakerId", exception.actualCiderMakerId().value().toString());

    return ResponseEntity
        .status(FORBIDDEN)
        .body(problem);
  }

  @ExceptionHandler(FeedstockMeasurementNotFoundException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleFeedstockMeasurementNotFound(
      @NonNull FeedstockMeasurementNotFoundException exception
  ) {
    final var problem = ProblemDetail.forStatus(NOT_FOUND);

    problem.setType(FEEDSTOCK_MEASUREMENT_NOT_FOUND);
    problem.setTitle("Feedstock measurement not found");
    problem.setDetail("Feedstock measurement does not exist: " + exception.measurementId().value());
    problem.setProperty("measurementId", exception.measurementId().value().toString());

    return ResponseEntity
        .status(NOT_FOUND)
        .body(problem);
  }

  @ExceptionHandler(FeedstockBatchHasNoMeasurementsException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleFeedstockBatchHasNoMeasurements(
      @NonNull FeedstockBatchHasNoMeasurementsException exception
  ) {
    final var problem = ProblemDetail.forStatus(UNPROCESSABLE_ENTITY);

    problem.setType(FEEDSTOCK_BATCH_HAS_NO_MEASUREMENTS);
    problem.setTitle("Feedstock batch has no measurements");
    problem.setDetail("Feedstock batch has no current chemical profile because no measurements have been recorded.");
    problem.setProperty("batchId", exception.batchId().value().toString());

    return ResponseEntity
        .status(UNPROCESSABLE_ENTITY)
        .body(problem);
  }

  @ExceptionHandler(InsufficientFeedstockCompositionSourcesException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleInsufficientFeedstockCompositionSources(
      @NonNull InsufficientFeedstockCompositionSourcesException exception
  ) {
    final var problem = ProblemDetail.forStatus(UNPROCESSABLE_ENTITY);

    problem.setType(INSUFFICIENT_FEEDSTOCK_COMPOSITION_SOURCES);
    problem.setTitle("Insufficient feedstock composition sources");
    problem.setDetail(
        "Feedstock composition optimization requires at least "
            + exception.minimumSourceCount()
            + " usable current batch profiles after filters are applied."
    );
    problem.setProperty("sourceCount", exception.sourceCount());
    problem.setProperty("minimumSourceCount", exception.minimumSourceCount());

    return ResponseEntity
        .status(UNPROCESSABLE_ENTITY)
        .body(problem);
  }
}
