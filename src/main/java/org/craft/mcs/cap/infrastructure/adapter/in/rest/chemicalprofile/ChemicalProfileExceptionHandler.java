package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile;


import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


@RestControllerAdvice
public final class ChemicalProfileExceptionHandler {

  private static final @NonNull URI CHEMICAL_PROFILE_CAPABILITY_NOT_MET =
      URI.create("urn:problem-type:chemical-profile-capability-not-met");
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

  private static @NonNull List<@NonNull String> axisNames(
      @NonNull Set<@NonNull ChemicalAxis> axes
  ) {
    return axes.stream()
        .sorted(Comparator.comparing(Enum::ordinal))
        .map(Enum::name)
        .toList();
  }
}
