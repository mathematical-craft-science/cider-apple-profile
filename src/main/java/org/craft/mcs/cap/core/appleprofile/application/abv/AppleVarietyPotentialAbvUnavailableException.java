package org.craft.mcs.cap.core.appleprofile.application.abv;

import java.io.Serial;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.capability.application.ProfileAnalysisInputUnavailableException;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisInputAvailability;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability.ABV_PREDICTION;


/** Indicates that an apple variety reference profile cannot support potential ABV prediction. */
@SuppressWarnings("java:S110")
public final class AppleVarietyPotentialAbvUnavailableException
    extends ProfileAnalysisInputUnavailableException {

  @Serial
  private static final long serialVersionUID = 1L;

  public AppleVarietyPotentialAbvUnavailableException(
      @NonNull AppleVarietyId varietyId,
      @NonNull ProfileAnalysisInputAvailability availability
  ) {
    super(
        "apple variety requires Specific Gravity for potential ABV prediction: "
            + requireNonNull(varietyId, "apple variety id cannot be null").value(),
        ABV_PREDICTION,
        availability
    );
  }
}
