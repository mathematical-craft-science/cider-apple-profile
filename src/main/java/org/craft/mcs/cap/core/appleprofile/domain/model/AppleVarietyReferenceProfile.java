package org.craft.mcs.cap.core.appleprofile.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;


/**
 * Catalog-level characteristic ranges known for an apple variety.
 *
 * <p>The profile contains pomological reference characteristics only. Harvest-specific evidence,
 * YAN and production measurements belong to other bounded contexts.</p>
 */
@ValueObject
public record AppleVarietyReferenceProfile(
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<PhValue> ph,
    @Nullable CharacteristicValue<TanninValue> tannin
) {

  public AppleVarietyReferenceProfile {
    if (specificGravity == null && titratableAcidity == null && ph == null && tannin == null) {
      throw new DomainRuleViolationException("apple variety reference profile cannot be empty");
    }
  }
}
