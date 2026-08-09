package org.craft.mcs.cap.core.appleprofile.application.classification;

import java.io.Serial;
import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.input.application.ProfileAnalysisInputUnavailableException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.TANNIN;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.TITRATABLE_ACIDITY;


/** Indicates that an apple variety lacks characteristics required by the North American profile. */
@SuppressWarnings("java:S110")
public final class AppleVarietyNorthAmericanProfileUnavailableException
    extends ProfileAnalysisInputUnavailableException {

  @Serial
  private static final long serialVersionUID = 1L;

  public AppleVarietyNorthAmericanProfileUnavailableException(
      @NonNull AppleVarietyId varietyId,
      @NonNull Set<@NonNull MeasurementCharacteristic> presentCharacteristics
  ) {
    super(
        "apple variety requires Specific Gravity, Titratable Acidity and tannin "
            + "for North American profile classification: "
            + requireNonNull(varietyId, "apple variety id cannot be null").value(),
        "NORTH_AMERICAN_CLASSIFICATION",
        Set.of(SPECIFIC_GRAVITY, TITRATABLE_ACIDITY, TANNIN),
        presentCharacteristics
    );
  }
}
