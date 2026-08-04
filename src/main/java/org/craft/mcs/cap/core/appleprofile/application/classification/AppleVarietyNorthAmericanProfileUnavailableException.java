package org.craft.mcs.cap.core.appleprofile.application.classification;

import java.io.Serial;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.capability.application.ProfileAnalysisInputUnavailableException;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisInputAvailability;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability.NORTH_AMERICAN_PROFILE;


/** Indicates that an apple variety lacks characteristics required by the North American profile. */
@SuppressWarnings("java:S110")
public final class AppleVarietyNorthAmericanProfileUnavailableException
    extends ProfileAnalysisInputUnavailableException {

  @Serial
  private static final long serialVersionUID = 1L;

  public AppleVarietyNorthAmericanProfileUnavailableException(
      @NonNull AppleVarietyId varietyId,
      @NonNull ProfileAnalysisInputAvailability availability
  ) {
    super(
        "apple variety requires Specific Gravity, Titratable Acidity and tannin "
            + "for North American profile classification: "
            + requireNonNull(varietyId, "apple variety id cannot be null").value(),
        NORTH_AMERICAN_PROFILE,
        availability
    );
  }
}
