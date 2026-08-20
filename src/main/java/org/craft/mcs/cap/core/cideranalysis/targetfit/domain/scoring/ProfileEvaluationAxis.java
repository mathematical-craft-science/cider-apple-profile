package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring;

import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Canonical dimensions admitted into profile-target distance and contribution-role analysis.
 */
public enum ProfileEvaluationAxis {

  /**
   * Specific gravity coordinate used by the profile evaluation objective space.
   */
  SPECIFIC_GRAVITY(MeasurementCharacteristic.SPECIFIC_GRAVITY),

  /**
   * Titratable acidity coordinate used by the profile evaluation objective space.
   */
  TITRATABLE_ACIDITY(MeasurementCharacteristic.TITRATABLE_ACIDITY),

  /**
   * Optional tannin coordinate used by extended profile target scoring.
   */
  TANNIN(MeasurementCharacteristic.TANNIN);

  private final @NonNull MeasurementCharacteristic characteristic;

  ProfileEvaluationAxis(@NonNull MeasurementCharacteristic characteristic) {
    this.characteristic = requireNonNull(characteristic, "measurement characteristic cannot be null");
  }

  /** Verifies that a canonical measured value belongs to this scoring dimension. */
  public boolean supports(@NonNull MeasuredValue value) {
    return characteristic.supports(value);
  }
}
