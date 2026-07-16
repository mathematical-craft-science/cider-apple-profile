package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out;

import java.util.List;
import org.jspecify.annotations.NonNull;


public interface AppleVarietySugarAcidityPointCandidateRepository {

  @NonNull
  List<@NonNull AppleVarietySugarAcidityPointCandidateView> findCandidates(
      @NonNull AppleVarietySugarAcidityPointCandidateQuery query
  );
}
