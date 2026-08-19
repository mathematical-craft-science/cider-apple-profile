package org.craft.mcs.cap.core.appleprofile.catalog.application.port.in;

import org.jspecify.annotations.NonNull;


public interface CreateAppleVarietyUseCase {

  @NonNull
  AppleVarietyCreated create(@NonNull CreateAppleVarietyCommand command);
}
