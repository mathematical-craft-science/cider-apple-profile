package org.craft.mcs.cap.core.appleprofile.application.catalog.port.out;

import java.util.List;
import org.jspecify.annotations.NonNull;


public interface AppleVarietyAnalysisCandidateQuery {

  @NonNull
  List<@NonNull AppleVarietyAnalysisCandidateView> findCandidates(
      @NonNull AppleVarietyAnalysisCandidateCriteria criteria
  );
}
