package org.craft.mcs.cap.core.profileanalysis.capability.domain;

import java.util.EnumSet;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Availability facts for the canonical characteristics used by profile-analysis scenarios.
 *
 * <p>The value contains no source-context types and no interpretation result. It is the minimal
 * bridge from AppleVariety or Feedstock data to reusable capability rules.</p>
 */
public record ProfileAnalysisInputAvailability(
    boolean specificGravity,
    boolean titratableAcidity,
    boolean tannin
) {

  /** Reports whether the available inputs satisfy one analysis scenario. */
  public boolean supports(@NonNull ProfileAnalysisCapability capability) {
    return presentCharacteristics().containsAll(
        requireNonNull(capability, "profile analysis capability cannot be null").requiredCharacteristics()
    );
  }

  /** Returns the canonical characteristics that are currently available. */
  public @NonNull Set<@NonNull MeasurementCharacteristic> presentCharacteristics() {
    final var present = EnumSet.noneOf(MeasurementCharacteristic.class);
    if (specificGravity) {
      present.add(MeasurementCharacteristic.SPECIFIC_GRAVITY);
    }
    if (titratableAcidity) {
      present.add(MeasurementCharacteristic.TITRATABLE_ACIDITY);
    }
    if (tannin) {
      present.add(MeasurementCharacteristic.TANNIN);
    }
    return Set.copyOf(present);
  }
}
