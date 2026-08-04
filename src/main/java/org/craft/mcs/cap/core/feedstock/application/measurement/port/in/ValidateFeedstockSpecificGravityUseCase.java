package org.craft.mcs.cap.core.feedstock.application.measurement.port.in;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/**
 * Inbound boundary for validating a canonical SG value against feedstock-stage rules.
 */
public interface ValidateFeedstockSpecificGravityUseCase {

  void validate(@NonNull SpecificGravityValue specificGravity);
}
