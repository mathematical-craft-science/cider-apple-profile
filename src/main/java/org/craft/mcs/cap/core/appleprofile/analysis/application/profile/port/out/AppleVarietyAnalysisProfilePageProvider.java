package org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPageQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.jspecify.annotations.NonNull;


/** Consumer-owned port for obtaining ordered pages of apple variety analysis inputs. */
public interface AppleVarietyAnalysisProfilePageProvider {

  @NonNull
  List<@NonNull AppleVarietyAnalysisProfile> findProfiles(
      @NonNull AppleVarietyAnalysisPageQuery query
  );
}
