package org.craft.mcs.cap.core.profileanalysis.input.application;

import java.io.Serial;
import java.util.Set;
import java.util.stream.Collectors;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Application failure exposing measurement-input facts required by one concrete analysis.
 *
 * <p>The failure deliberately carries no universal capability model. Each use case names its own
 * analysis and supplies only the characteristics relevant to that analysis.</p>
 */
@SuppressWarnings("java:S110")
public abstract class ProfileAnalysisInputUnavailableException
    extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final @NonNull String analysis;
  private final @NonNull Set<@NonNull MeasurementCharacteristic> requiredCharacteristics;
  private final @NonNull Set<@NonNull MeasurementCharacteristic> presentCharacteristics;

  protected ProfileAnalysisInputUnavailableException(
      @NonNull String message,
      @NonNull String analysis,
      @NonNull Set<@NonNull MeasurementCharacteristic> requiredCharacteristics,
      @NonNull Set<@NonNull MeasurementCharacteristic> presentCharacteristics
  ) {
    super(message);
    this.analysis = requireNonNull(analysis, "profile analysis name cannot be null");
    this.requiredCharacteristics = Set.copyOf(
        requireNonNull(requiredCharacteristics, "required analysis characteristics cannot be null")
    );
    this.presentCharacteristics = Set.copyOf(
        requireNonNull(presentCharacteristics, "present analysis characteristics cannot be null")
    );
  }

  public final @NonNull String analysis() {
    return analysis;
  }

  public final @NonNull Set<@NonNull MeasurementCharacteristic> requiredCharacteristics() {
    return requiredCharacteristics;
  }

  public final @NonNull Set<@NonNull MeasurementCharacteristic> presentCharacteristics() {
    return presentCharacteristics;
  }

  public final @NonNull Set<@NonNull MeasurementCharacteristic> missingCharacteristics() {
    return requiredCharacteristics.stream()
        .filter(characteristic -> !presentCharacteristics.contains(characteristic))
        .collect(Collectors.toUnmodifiableSet());
  }
}
