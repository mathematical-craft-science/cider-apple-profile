package org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract;

import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Analysis-owned input assembled from an immutable catalog reference profile. */
public record AppleVarietyAnalysisProfile(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyAnalysisName name,
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<PhValue> ph,
    @Nullable CharacteristicValue<TanninValue> tannin
) {

  public AppleVarietyAnalysisProfile {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(name, "apple variety name cannot be null");
  }
}
