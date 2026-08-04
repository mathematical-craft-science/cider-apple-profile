package org.craft.mcs.cap.core.profileanalysis.graph.application.port.in;

import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Inbound profile-analysis boundary for projecting typed SG and TA onto the Jolicoeur graph. */
public interface EvaluateJolicoeurSugarAcidityGraphUseCase {

  @NonNull JolicoeurSugarAcidityGraphEvaluation evaluate(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  );
}
