package org.craft.mcs.cap.core.appleprofile.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


public interface AppleVarietyRepository {

  @NonNull
  AppleVariety save(@NonNull AppleVariety appleVariety);

  @NonNull
  Optional<@NonNull AppleVariety> findById(@NonNull AppleVarietyId varietyId);

  @NonNull
  List<@NonNull AppleVariety> findAll();
}
