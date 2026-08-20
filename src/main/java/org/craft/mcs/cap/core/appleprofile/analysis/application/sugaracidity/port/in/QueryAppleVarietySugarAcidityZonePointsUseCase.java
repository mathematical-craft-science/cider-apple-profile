package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPagePosition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.contract.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;


public interface QueryAppleVarietySugarAcidityZonePointsUseCase {

  @NonNull
  CursorPage<@NonNull AppleVarietyGraphAnalysisView, @NonNull AppleVarietyAnalysisPagePosition> query(
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  );
}
