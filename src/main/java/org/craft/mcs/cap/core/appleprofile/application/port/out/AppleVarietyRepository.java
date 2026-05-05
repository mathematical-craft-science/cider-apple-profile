package org.craft.mcs.cap.core.appleprofile.application.port.out;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.jspecify.annotations.NonNull;


public interface AppleVarietyRepository {

  @NonNull
  AppleVariety save(@NonNull AppleVariety appleVariety);
}
