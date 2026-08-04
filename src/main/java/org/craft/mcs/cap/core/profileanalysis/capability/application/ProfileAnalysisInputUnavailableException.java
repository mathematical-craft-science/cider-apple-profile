package org.craft.mcs.cap.core.profileanalysis.capability.application;

import java.io.Serial;
import java.util.EnumSet;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisInputAvailability;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Base application failure carrying typed facts about inputs unavailable to one analysis scenario.
 */
@SuppressWarnings("java:S110")
public abstract class ProfileAnalysisInputUnavailableException
    extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final @NonNull ProfileAnalysisCapability capability;
  private final @NonNull Set<@NonNull MeasurementCharacteristic> requiredCharacteristics;
  private final @NonNull Set<@NonNull MeasurementCharacteristic> presentCharacteristics;

  protected ProfileAnalysisInputUnavailableException(
      @NonNull String message,
      @NonNull ProfileAnalysisCapability capability,
      @NonNull ProfileAnalysisInputAvailability availability
  ) {
    super(message);
    this.capability = requireNonNull(capability, "profile analysis capability cannot be null");
    this.requiredCharacteristics = capability.requiredCharacteristics();
    this.presentCharacteristics = requireNonNull(
        availability,
        "profile analysis input availability cannot be null"
    ).presentCharacteristics();
  }

  public final @NonNull ProfileAnalysisCapability capability() {
    return capability;
  }

  public final @NonNull Set<@NonNull MeasurementCharacteristic> requiredCharacteristics() {
    return requiredCharacteristics;
  }

  public final @NonNull Set<@NonNull MeasurementCharacteristic> presentCharacteristics() {
    return presentCharacteristics;
  }

  public final @NonNull Set<@NonNull MeasurementCharacteristic> missingCharacteristics() {
    final var missing = EnumSet.copyOf(requiredCharacteristics);
    missing.removeAll(presentCharacteristics);
    return Set.copyOf(missing);
  }

}
