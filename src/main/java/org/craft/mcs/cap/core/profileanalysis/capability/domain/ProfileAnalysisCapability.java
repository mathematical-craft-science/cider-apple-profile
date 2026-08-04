package org.craft.mcs.cap.core.profileanalysis.capability.domain;

import java.util.Set;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Analysis scenarios supported by the available canonical profile characteristics.
 */
public enum ProfileAnalysisCapability {

  /**
   * Specific Gravity and Titratable Acidity are available for two-dimensional graph projection.
   */
  SUGAR_ACIDITY_GRAPH(Set.of(
      MeasurementCharacteristic.SPECIFIC_GRAVITY,
      MeasurementCharacteristic.TITRATABLE_ACIDITY
  )),

  /**
   * Specific Gravity, Titratable Acidity and tannin are available for North American classification.
   */
  NORTH_AMERICAN_PROFILE(Set.of(
      MeasurementCharacteristic.SPECIFIC_GRAVITY,
      MeasurementCharacteristic.TITRATABLE_ACIDITY,
      MeasurementCharacteristic.TANNIN
  )),

  /**
   * Specific Gravity is available for potential ABV prediction.
   */
  ABV_PREDICTION(Set.of(MeasurementCharacteristic.SPECIFIC_GRAVITY));

  private final @NonNull Set<@NonNull MeasurementCharacteristic> requiredCharacteristics;

  ProfileAnalysisCapability(
      @NonNull Set<@NonNull MeasurementCharacteristic> requiredCharacteristics
  ) {
    this.requiredCharacteristics = Set.copyOf(
        requireNonNull(requiredCharacteristics, "required profile characteristics cannot be null")
    );
  }

  /** Returns the canonical characteristics required by this analysis scenario. */
  public @NonNull Set<@NonNull MeasurementCharacteristic> requiredCharacteristics() {
    return requiredCharacteristics;
  }
}
