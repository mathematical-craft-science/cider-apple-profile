package org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.PhPlanningGuidanceView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Assesses pH planning guidance without blocking a production decision. */
public interface AssessPhPlanningGuidanceUseCase {

  @NonNull
  PhPlanningGuidanceView assess(@Nullable CharacteristicValue<PhValue> ph);
}
