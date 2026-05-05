package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.jspecify.annotations.NonNull;

public interface CreateAppleVarietyUseCase {

  @NonNull
  AppleVariety create(@NonNull CreateAppleVarietyCommand command);
}
