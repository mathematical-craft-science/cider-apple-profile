package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement;


import java.net.URI;
import org.craft.mcs.cap.shared.application.UnsupportedMeasurementUnitException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


@RestControllerAdvice
public final class MeasurementUnitExceptionHandler {

  private static final @NonNull URI UNSUPPORTED_CHEMICAL_MEASUREMENT_UNIT =
      URI.create("urn:problem-type:unsupported-chemical-measurement-unit");
  private static final int BAD_REQUEST = 400;

  @ExceptionHandler(UnsupportedMeasurementUnitException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleUnsupportedChemicalMeasurementUnit(
      @NonNull UnsupportedMeasurementUnitException exception
  ) {
    return response(
        BAD_REQUEST,
        UNSUPPORTED_CHEMICAL_MEASUREMENT_UNIT,
        "Unsupported chemical measurement unit",
        exception.getMessage()
    );
  }
}
