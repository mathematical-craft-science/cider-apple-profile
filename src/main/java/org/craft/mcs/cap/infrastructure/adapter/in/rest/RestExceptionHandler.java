package org.craft.mcs.cap.infrastructure.adapter.in.rest;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public final class RestExceptionHandler {

  private static final URI CHEMICAL_PROFILE_CAPABILITY_NOT_MET =
      URI.create("urn:problem-type:chemical-profile-capability-not-met");
  private static final URI FEEDSTOCK_BATCH_NOT_FOUND =
      URI.create("urn:problem-type:feedstock-batch-not-found");
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(ChemicalProfileCapabilityNotMetException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleChemicalProfileCapabilityNotMet(
      @NonNull ChemicalProfileCapabilityNotMetException exception
  ) {
    final var assessment = exception.assessment();
    final var problem = ProblemDetail.forStatus(UNPROCESSABLE_ENTITY);

    problem.setType(CHEMICAL_PROFILE_CAPABILITY_NOT_MET);
    problem.setTitle("Chemical profile does not meet capability requirements");
    problem.setDetail("Chemical profile is missing measurements required for " + assessment.capability().name());
    problem.setProperty("capability", assessment.capability().name());
    problem.setProperty("requiredAxes", axisNames(assessment.requiredAxes()));
    problem.setProperty("presentAxes", axisNames(assessment.presentAxes()));
    problem.setProperty("missingAxes", axisNames(assessment.missingAxes()));

    return ResponseEntity
        .status(UNPROCESSABLE_ENTITY)
        .body(problem);
  }

  @ExceptionHandler(FeedstockBatchNotFoundException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleFeedstockBatchNotFound(
      @NonNull FeedstockBatchNotFoundException exception
  ) {
    final var problem = ProblemDetail.forStatus(404);

    problem.setType(FEEDSTOCK_BATCH_NOT_FOUND);
    problem.setTitle("Feedstock batch not found");
    problem.setDetail("Feedstock batch does not exist: " + exception.batchId().value());
    problem.setProperty("batchId", exception.batchId().value().toString());

    return ResponseEntity
        .status(404)
        .body(problem);
  }

  private static @NonNull List<@NonNull String> axisNames(
      @NonNull Set<@NonNull ChemicalAxis> axes
  ) {
    return axes.stream()
        .sorted(Comparator.comparing(Enum::ordinal))
        .map(Enum::name)
        .toList();
  }
}
