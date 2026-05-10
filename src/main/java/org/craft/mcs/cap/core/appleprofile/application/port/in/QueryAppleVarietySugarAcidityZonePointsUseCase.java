package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.jspecify.annotations.NonNull;


public interface QueryAppleVarietySugarAcidityZonePointsUseCase {

  @NonNull
  CursorPage<@NonNull AppleVarietySugarAcidityZoneResult> query(
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  );
}
