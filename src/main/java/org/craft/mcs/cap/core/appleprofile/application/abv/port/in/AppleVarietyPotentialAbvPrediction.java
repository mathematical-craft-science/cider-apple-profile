package org.craft.mcs.cap.core.appleprofile.application.abv.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyPotentialAbvPrediction(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @NonNull CharacteristicValue<SpecificGravityValue> originalGravity,
    @NonNull PotentialAbvPrediction prediction
) {

  public AppleVarietyPotentialAbvPrediction {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(prediction, "potential ABV prediction cannot be null");
  }
}
