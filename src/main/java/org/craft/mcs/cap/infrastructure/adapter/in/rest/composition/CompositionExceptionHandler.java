package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition;

import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice(assignableTypes = ChemicalProfileCompositionController.class)
public final class CompositionExceptionHandler {

  private static final @NonNull URI INVALID_CHEMICAL_PROFILE_COMPOSITION_CALCULATION =
      URI.create("urn:problem-type:invalid-chemical-profile-composition-calculation");
  private static final int BAD_REQUEST = 400;

  @ExceptionHandler(IllegalArgumentException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleIllegalArgument(
      @NonNull IllegalArgumentException exception
  ) {
    final var problem = ProblemDetail.forStatus(BAD_REQUEST);

    problem.setType(INVALID_CHEMICAL_PROFILE_COMPOSITION_CALCULATION);
    problem.setTitle("Invalid chemical profile composition calculation");
    problem.setDetail(exception.getMessage());

    return ResponseEntity
        .status(BAD_REQUEST)
        .body(problem);
  }
}
