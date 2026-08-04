package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.error;


import java.net.URI;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.FeedstockBatchPotentialAbvController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.FeedstockBatchController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.FeedstockBatchCompositionPlanningController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.FeedstockBatchReadinessController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.FeedstockBatchSugarAcidityController;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.create;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


@RestControllerAdvice(assignableTypes = {
    FeedstockBatchController.class,
    FeedstockBatchCompositionPlanningController.class,
    FeedstockBatchPotentialAbvController.class,
    FeedstockBatchReadinessController.class,
    FeedstockBatchSugarAcidityController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class FeedstockExceptionHandler {

  private static final @NonNull URI FEEDSTOCK_BATCH_NOT_FOUND =
      URI.create("urn:problem-type:feedstock-batch-not-found");
  private static final @NonNull URI FEEDSTOCK_BATCH_HAS_NO_MEASUREMENTS =
      URI.create("urn:problem-type:feedstock-batch-has-no-measurements");
  private static final @NonNull URI FEEDSTOCK_BATCH_OWNERSHIP_MISMATCH =
      URI.create("urn:problem-type:feedstock-batch-ownership-mismatch");
  private static final @NonNull URI INSUFFICIENT_FEEDSTOCK_COMPOSITION_SOURCES =
      URI.create("urn:problem-type:insufficient-feedstock-composition-sources");
  private static final int NOT_FOUND = 404;
  private static final int FORBIDDEN = 403;
  private static final int UNPROCESSABLE_ENTITY = 422;
  private static final @NonNull String BATCH_ID_PROPERTY = "batchId";

  @ExceptionHandler(FeedstockBatchNotFoundException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleFeedstockBatchNotFound(
      @NonNull FeedstockBatchNotFoundException exception
  ) {
    final var problem = create(
        NOT_FOUND,
        FEEDSTOCK_BATCH_NOT_FOUND,
        "Feedstock batch not found",
        "Feedstock batch does not exist: " + exception.batchId().value()
    );
    problem.setProperty(BATCH_ID_PROPERTY, exception.batchId().value().toString());

    return response(NOT_FOUND, problem);
  }

  @ExceptionHandler(FeedstockBatchOwnershipMismatchException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleFeedstockBatchOwnershipMismatch(
      @NonNull FeedstockBatchOwnershipMismatchException exception
  ) {
    final var problem = create(
        FORBIDDEN,
        FEEDSTOCK_BATCH_OWNERSHIP_MISMATCH,
        "Feedstock batch belongs to another workspace",
        "Feedstock batch cannot be modified by this workspace."
    );
    problem.setProperty(BATCH_ID_PROPERTY, exception.batchId().value().toString());
    problem.setProperty("expectedWorkspaceId", exception.expectedWorkspaceId().value().toString());
    problem.setProperty("actualWorkspaceId", exception.actualWorkspaceId().value().toString());

    return response(FORBIDDEN, problem);
  }

  @ExceptionHandler(FeedstockBatchHasNoMeasurementsException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleFeedstockBatchHasNoMeasurements(
      @NonNull FeedstockBatchHasNoMeasurementsException exception
  ) {
    final var problem = create(
        UNPROCESSABLE_ENTITY,
        FEEDSTOCK_BATCH_HAS_NO_MEASUREMENTS,
        "Feedstock batch has no measurements",
        "Feedstock batch has no current measured characteristics."
    );
    problem.setProperty(BATCH_ID_PROPERTY, exception.batchId().value().toString());

    return response(UNPROCESSABLE_ENTITY, problem);
  }

  @ExceptionHandler(InsufficientFeedstockCompositionSourcesException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleInsufficientFeedstockCompositionSources(
      @NonNull InsufficientFeedstockCompositionSourcesException exception
  ) {
    final var problem = create(
        UNPROCESSABLE_ENTITY,
        INSUFFICIENT_FEEDSTOCK_COMPOSITION_SOURCES,
        "Insufficient feedstock composition sources",
        "Feedstock composition optimization requires at least "
            + exception.minimumSourceCount()
            + " usable current batch profiles after filters are applied."
    );
    problem.setProperty("sourceCount", exception.sourceCount());
    problem.setProperty("minimumSourceCount", exception.minimumSourceCount());

    return response(UNPROCESSABLE_ENTITY, problem);
  }
}
