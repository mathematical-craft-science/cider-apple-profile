package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import java.net.URI;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceInsufficientVolumeException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceNotFoundException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceProfileUnavailableException;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
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
    ProductionBatchController.class,
    ProductionBatchCandidateController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ProductionExceptionHandler {

  private static final @NonNull URI SOURCE_OWNERSHIP_MISMATCH =
      URI.create("urn:problem-type:production-source-ownership-mismatch");
  private static final @NonNull URI SOURCE_NOT_FOUND =
      URI.create("urn:problem-type:production-source-not-found");
  private static final @NonNull URI INSUFFICIENT_AVAILABLE_VOLUME =
      URI.create("urn:problem-type:production-source-insufficient-available-volume");
  private static final @NonNull URI SOURCE_PROFILE_UNAVAILABLE =
      URI.create("urn:problem-type:production-source-profile-unavailable");
  private static final int NOT_FOUND = 404;
  private static final int FORBIDDEN = 403;
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(ProductionSourceNotFoundException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleSourceNotFound(
      @NonNull ProductionSourceNotFoundException exception
  ) {
    return sourceNotFound(exception.sourceReference());
  }

  @ExceptionHandler(ProductionPlanningSourceNotFoundException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handlePlanningSourceNotFound(
      @NonNull ProductionPlanningSourceNotFoundException exception
  ) {
    return sourceNotFound(exception.sourceReference());
  }

  private static @NonNull ResponseEntity<@NonNull ProblemDetail> sourceNotFound(
      @NonNull ProductionSourceReference sourceReference
  ) {
    final var problem = create(
        NOT_FOUND,
        SOURCE_NOT_FOUND,
        "Production source not found",
        "The referenced production source does not exist."
    );
    addSourceProperties(problem, sourceReference);

    return response(NOT_FOUND, problem);
  }

  @ExceptionHandler(ProductionSourceOwnershipMismatchException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleOwnershipMismatch(
      @NonNull ProductionSourceOwnershipMismatchException exception
  ) {
    return ownershipMismatch(
        exception.sourceReference(),
        exception.expectedWorkspaceId(),
        exception.actualWorkspaceId()
    );
  }

  @ExceptionHandler(ProductionPlanningSourceOwnershipMismatchException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handlePlanningOwnershipMismatch(
      @NonNull ProductionPlanningSourceOwnershipMismatchException exception
  ) {
    return ownershipMismatch(
        exception.sourceReference(),
        exception.expectedWorkspaceId(),
        exception.actualWorkspaceId()
    );
  }

  private static @NonNull ResponseEntity<@NonNull ProblemDetail> ownershipMismatch(
      @NonNull ProductionSourceReference sourceReference,
      @NonNull WorkspaceId expectedWorkspaceId,
      @NonNull WorkspaceId actualWorkspaceId
  ) {
    final var problem = create(
        FORBIDDEN,
        SOURCE_OWNERSHIP_MISMATCH,
        "Production source belongs to another workspace",
        "Production source cannot be used by this workspace."
    );
    addSourceProperties(problem, sourceReference);
    problem.setProperty("expectedWorkspaceId", expectedWorkspaceId.value().toString());
    problem.setProperty("actualWorkspaceId", actualWorkspaceId.value().toString());

    return response(FORBIDDEN, problem);
  }

  @ExceptionHandler(ProductionSourceInsufficientAvailableVolumeException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleInsufficientVolume(
      @NonNull ProductionSourceInsufficientAvailableVolumeException exception
  ) {
    return insufficientVolume(
        exception.sourceReference(),
        exception.requestedVolume(),
        exception.availableVolume()
    );
  }

  @ExceptionHandler(ProductionPlanningSourceInsufficientVolumeException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handlePlanningInsufficientVolume(
      @NonNull ProductionPlanningSourceInsufficientVolumeException exception
  ) {
    return insufficientVolume(
        exception.sourceReference(),
        exception.requestedVolume(),
        exception.availableVolume()
    );
  }

  private static @NonNull ResponseEntity<@NonNull ProblemDetail> insufficientVolume(
      @NonNull ProductionSourceReference sourceReference,
      @NonNull VolumeValue requestedVolume,
      @NonNull VolumeValue availableVolume
  ) {
    final var problem = create(
        UNPROCESSABLE_ENTITY,
        INSUFFICIENT_AVAILABLE_VOLUME,
        "Insufficient available source volume",
        "Requested production source volume exceeds available source volume."
    );
    addSourceProperties(problem, sourceReference);
    problem.setProperty(
        "requestedVolume",
        new DecimalQuantityResponse(requestedVolume.liters(), CanonicalUnitLabels.VOLUME)
    );
    problem.setProperty(
        "availableVolume",
        new DecimalQuantityResponse(availableVolume.liters(), CanonicalUnitLabels.VOLUME)
    );

    return response(UNPROCESSABLE_ENTITY, problem);
  }

  @ExceptionHandler(ProductionSourceProfileUnavailableException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleSourceProfileUnavailable(
      @NonNull ProductionSourceProfileUnavailableException exception
  ) {
    return sourceProfileUnavailable(exception.sourceReference());
  }

  @ExceptionHandler(ProductionPlanningSourceProfileUnavailableException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handlePlanningSourceProfileUnavailable(
      @NonNull ProductionPlanningSourceProfileUnavailableException exception
  ) {
    return sourceProfileUnavailable(exception.sourceReference());
  }

  private static @NonNull ResponseEntity<@NonNull ProblemDetail> sourceProfileUnavailable(
      @NonNull ProductionSourceReference sourceReference
  ) {
    final var problem = create(
        UNPROCESSABLE_ENTITY,
        SOURCE_PROFILE_UNAVAILABLE,
        "Production source profile unavailable",
        "The referenced production source has no current characteristics for composition."
    );
    addSourceProperties(problem, sourceReference);

    return response(UNPROCESSABLE_ENTITY, problem);
  }

  private static void addSourceProperties(
      @NonNull ProblemDetail problem,
      @NonNull ProductionSourceReference sourceReference
  ) {
    problem.setProperty("sourceType", sourceReference.type().name());
    problem.setProperty("sourceId", sourceReference.id().toString());
  }
}
