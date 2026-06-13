package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.jspecify.annotations.NonNull;


public interface AppleVarietySugarAcidityPointCandidateRepository {

  @NonNull
  List<@NonNull AppleVariety> findCandidates(@NonNull AppleVarietySugarAcidityPointCandidateQuery query);
}
