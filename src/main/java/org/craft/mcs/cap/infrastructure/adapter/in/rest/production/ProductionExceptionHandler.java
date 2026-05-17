package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import java.net.URI;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice(assignableTypes = ProductionBatchController.class)
public final class ProductionExceptionHandler {

  private static final @NonNull URI SOURCE_OWNERSHIP_MISMATCH =
      URI.create("urn:problem-type:production-source-ownership-mismatch");
  private static final @NonNull URI INSUFFICIENT_AVAILABLE_VOLUME =
      URI.create("urn:problem-type:production-source-insufficient-available-volume");
  private static final @NonNull URI INVALID_PRODUCTION_BATCH =
      URI.create("urn:problem-type:invalid-production-batch");
  private static final int BAD_REQUEST = 400;
  private static final int FORBIDDEN = 403;
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(ProductionSourceOwnershipMismatchException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleOwnershipMismatch(
      @NonNull ProductionSourceOwnershipMismatchException exception
  ) {
    final var problem = ProblemDetail.forStatus(FORBIDDEN);

    problem.setType(SOURCE_OWNERSHIP_MISMATCH);
    problem.setTitle("Production source belongs to another cider maker");
    problem.setDetail("Feedstock batch cannot be used by this cider maker.");
    problem.setProperty("batchId", exception.batchId().value().toString());
    problem.setProperty("expectedCiderMakerId", exception.expectedCiderMakerId().value().toString());
    problem.setProperty("actualCiderMakerId", exception.actualCiderMakerId().value().toString());

    return ResponseEntity
        .status(FORBIDDEN)
        .body(problem);
  }

  @ExceptionHandler(ProductionSourceInsufficientAvailableVolumeException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleInsufficientVolume(
      @NonNull ProductionSourceInsufficientAvailableVolumeException exception
  ) {
    final var problem = ProblemDetail.forStatus(UNPROCESSABLE_ENTITY);

    problem.setType(INSUFFICIENT_AVAILABLE_VOLUME);
    problem.setTitle("Insufficient available feedstock volume");
    problem.setDetail("Requested production source volume exceeds available feedstock batch volume.");
    problem.setProperty("batchId", exception.batchId().value().toString());
    problem.setProperty("requestedVolumeLiters", exception.requestedVolume().liters());
    problem.setProperty("availableVolumeLiters", exception.availableVolume().liters());

    return ResponseEntity
        .status(UNPROCESSABLE_ENTITY)
        .body(problem);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleIllegalArgument(
      @NonNull IllegalArgumentException exception
  ) {
    final var problem = ProblemDetail.forStatus(BAD_REQUEST);

    problem.setType(INVALID_PRODUCTION_BATCH);
    problem.setTitle("Invalid production batch");
    problem.setDetail(exception.getMessage());

    return ResponseEntity
        .status(BAD_REQUEST)
        .body(problem);
  }
}
