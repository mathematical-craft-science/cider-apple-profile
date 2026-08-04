package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.capability.application.ProfileAnalysisInputUnavailableException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.create;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;


/** Preserves explainable RFC 9457 errors for unavailable profile-analysis inputs. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ProfileAnalysisInputExceptionHandler {

  private static final @NonNull URI PROFILE_ANALYSIS_INPUT_UNAVAILABLE =
      URI.create("urn:problem-type:profile-analysis-input-unavailable");
  private static final int UNPROCESSABLE_ENTITY = 422;

  @ExceptionHandler(ProfileAnalysisInputUnavailableException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleInputUnavailable(
      @NonNull ProfileAnalysisInputUnavailableException exception
  ) {
    final var problem = create(
        UNPROCESSABLE_ENTITY,
        PROFILE_ANALYSIS_INPUT_UNAVAILABLE,
        "Profile analysis inputs are unavailable",
        "Current characteristics do not satisfy " + exception.capability().name()
    );
    problem.setProperty("capability", exception.capability().name());
    problem.setProperty(
        "requiredCharacteristics",
        characteristicNames(exception.requiredCharacteristics())
    );
    problem.setProperty(
        "presentCharacteristics",
        characteristicNames(exception.presentCharacteristics())
    );
    problem.setProperty(
        "missingCharacteristics",
        characteristicNames(exception.missingCharacteristics())
    );

    return response(UNPROCESSABLE_ENTITY, problem);
  }

  private static @NonNull List<@NonNull String> characteristicNames(
      @NonNull Set<@NonNull MeasurementCharacteristic> characteristics
  ) {
    return characteristics.stream()
        .sorted(Comparator.comparing(Enum::ordinal))
        .map(Enum::name)
        .toList();
  }
}
