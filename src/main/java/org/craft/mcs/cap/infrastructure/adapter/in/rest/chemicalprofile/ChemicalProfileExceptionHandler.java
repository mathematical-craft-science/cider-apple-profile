package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile;


import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityNotMetException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.create;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


@RestControllerAdvice
public final class ChemicalProfileExceptionHandler {

  private static final @NonNull URI CHEMICAL_PROFILE_CAPABILITY_NOT_MET =
      URI.create("urn:problem-type:chemical-profile-capability-not-met");
  private static final @NonNull URI UNSUPPORTED_CHEMICAL_MEASUREMENT_UNIT =
      URI.create("urn:problem-type:unsupported-chemical-measurement-unit");
  private static final int BAD_REQUEST = 400;
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(ChemicalProfileCapabilityNotMetException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleChemicalProfileCapabilityNotMet(
      @NonNull ChemicalProfileCapabilityNotMetException exception
  ) {
    final var assessment = exception.assessment();
    final var problem = create(
        UNPROCESSABLE_ENTITY,
        CHEMICAL_PROFILE_CAPABILITY_NOT_MET,
        "Chemical profile does not meet capability requirements",
        "Chemical profile is missing measurements required for " + assessment.capability().name()
    );
    problem.setProperty("capability", assessment.capability().name());
    problem.setProperty("requiredAxes", axisNames(assessment.requiredAxes()));
    problem.setProperty("presentAxes", axisNames(assessment.presentAxes()));
    problem.setProperty("missingAxes", axisNames(assessment.missingAxes()));

    return response(UNPROCESSABLE_ENTITY, problem);
  }

  @ExceptionHandler(UnsupportedChemicalMeasurementUnitException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleUnsupportedChemicalMeasurementUnit(
      @NonNull UnsupportedChemicalMeasurementUnitException exception
  ) {
    return response(
        BAD_REQUEST,
        UNSUPPORTED_CHEMICAL_MEASUREMENT_UNIT,
        "Unsupported chemical measurement unit",
        exception.getMessage()
    );
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
