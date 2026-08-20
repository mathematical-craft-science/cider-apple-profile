package org.craft.mcs.cap.core.appleprofile.analysis.application.graph.port.out;

import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Shared AppleProfile Analysis outbound port for SG/TA graph evaluation. */
public interface AppleSugarAcidityGraphAnalyzer {

  @NonNull AppleSugarAcidityGraphView evaluate(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  );
}
