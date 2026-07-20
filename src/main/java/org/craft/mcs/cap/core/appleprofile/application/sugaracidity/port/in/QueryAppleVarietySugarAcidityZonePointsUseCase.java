package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietySugarAcidityPagePosition;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;


public interface QueryAppleVarietySugarAcidityZonePointsUseCase {

  @NonNull
  CursorPage<@NonNull AppleVarietySugarAcidityZoneResult, @NonNull AppleVarietySugarAcidityPagePosition> query(
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  );
}
