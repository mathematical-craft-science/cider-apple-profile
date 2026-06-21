package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import java.net.URI;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.create;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


@RestControllerAdvice(assignableTypes = ProductionBatchController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ProductionExceptionHandler {

  private static final @NonNull URI SOURCE_OWNERSHIP_MISMATCH =
      URI.create("urn:problem-type:production-source-ownership-mismatch");
  private static final @NonNull URI INSUFFICIENT_AVAILABLE_VOLUME =
      URI.create("urn:problem-type:production-source-insufficient-available-volume");
  private static final int FORBIDDEN = 403;
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(ProductionSourceOwnershipMismatchException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleOwnershipMismatch(
      @NonNull ProductionSourceOwnershipMismatchException exception
  ) {
    final var problem = create(
        FORBIDDEN,
        SOURCE_OWNERSHIP_MISMATCH,
        "Production source belongs to another cider maker",
        "Production source cannot be used by this cider maker."
    );
    addSourceProperties(problem, exception.sourceReference());
    problem.setProperty("expectedCiderMakerId", exception.expectedCiderMakerId().value().toString());
    problem.setProperty("actualCiderMakerId", exception.actualCiderMakerId().value().toString());

    return response(FORBIDDEN, problem);
  }

  @ExceptionHandler(ProductionSourceInsufficientAvailableVolumeException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleInsufficientVolume(
      @NonNull ProductionSourceInsufficientAvailableVolumeException exception
  ) {
    final var problem = create(
        UNPROCESSABLE_ENTITY,
        INSUFFICIENT_AVAILABLE_VOLUME,
        "Insufficient available source volume",
        "Requested production source volume exceeds available source volume."
    );
    addSourceProperties(problem, exception.sourceReference());
    problem.setProperty(
        "requestedVolume",
        new DecimalQuantityResponse(exception.requestedVolume().liters(), CanonicalUnitLabels.VOLUME)
    );
    problem.setProperty(
        "availableVolume",
        new DecimalQuantityResponse(exception.availableVolume().liters(), CanonicalUnitLabels.VOLUME)
    );

    return response(UNPROCESSABLE_ENTITY, problem);
  }

  private static void addSourceProperties(
      @NonNull ProblemDetail problem,
      @NonNull ProductionSourceReference sourceReference
  ) {
    problem.setProperty("sourceType", sourceReference.type().name());
    problem.setProperty("sourceId", sourceReference.id().toString());
    problem.setProperty("batchId", sourceReference.id().toString());
  }
}
