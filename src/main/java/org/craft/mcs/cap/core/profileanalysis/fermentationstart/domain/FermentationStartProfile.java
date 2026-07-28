package org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;


/**
 * Minimal typed evidence needed to assess readiness immediately before fermentation starts.
 *
 * <p>The profile intentionally excludes TA, tannin, temperature, and other characteristics that do
 * not participate in the current readiness rules. Missing characteristics remain explicit because
 * the assessor must report their absence.</p>
 */
@ValueObject
public record FermentationStartProfile(
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<PhValue> ph,
    @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
) {
}
