package org.craft.mcs.cap.core.appleprofile.catalog.application.contract;

import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Immutable published catalog projection of one apple variety reference profile.
 */
public record AppleVarietyReferenceProfileView(
    @NonNull AppleVarietyId varietyId,
    @NonNull String name,
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<PhValue> ph,
    @Nullable CharacteristicValue<TanninValue> tannin
) {

  public AppleVarietyReferenceProfileView {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(name, "apple variety name cannot be null");
    name = name.strip();
    if (name.isEmpty()) {
      throw new ApplicationValidationException("apple variety name cannot be blank");
    }
    if (specificGravity == null && titratableAcidity == null && ph == null && tannin == null) {
      throw new ApplicationValidationException("apple variety reference profile cannot be empty");
    }
  }
}
