package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.jspecify.annotations.NonNull;


public interface CreateAppleVarietyUseCase {

  @NonNull
  AppleVarietyCreated create(@NonNull CreateAppleVarietyCommand command);
}
