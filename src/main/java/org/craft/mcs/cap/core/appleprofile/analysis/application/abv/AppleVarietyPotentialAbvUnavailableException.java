package org.craft.mcs.cap.core.appleprofile.analysis.application.abv;

import java.io.Serial;
import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.input.application.ProfileAnalysisInputUnavailableException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;


/** Indicates that an apple variety reference profile cannot support potential ABV prediction. */
@SuppressWarnings("java:S110")
public final class AppleVarietyPotentialAbvUnavailableException
    extends ProfileAnalysisInputUnavailableException {

  @Serial
  private static final long serialVersionUID = 1L;

  public AppleVarietyPotentialAbvUnavailableException(
      @NonNull AppleVarietyId varietyId
  ) {
    super(
        "apple variety requires Specific Gravity for potential ABV prediction: "
            + requireNonNull(varietyId, "apple variety id cannot be null").value(),
        "POTENTIAL_ABV_PREDICTION",
        Set.of(SPECIFIC_GRAVITY),
        Set.<MeasurementCharacteristic>of()
    );
  }
}
