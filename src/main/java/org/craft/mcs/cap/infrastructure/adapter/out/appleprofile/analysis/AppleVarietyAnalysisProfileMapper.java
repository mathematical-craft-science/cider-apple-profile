package org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfileView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps the catalog published language into the analysis-owned input model. */
final class AppleVarietyAnalysisProfileMapper {

  private AppleVarietyAnalysisProfileMapper() {
  }

  static @NonNull AppleVarietyAnalysisProfile toAnalysisProfile(
      @NonNull AppleVarietyReferenceProfileView view
  ) {
    requireNonNull(view, "apple variety reference profile view cannot be null");
    return new AppleVarietyAnalysisProfile(
        view.varietyId(),
        AppleVarietyAnalysisName.of(view.name()),
        view.specificGravity(),
        view.titratableAcidity(),
        view.ph(),
        view.tannin()
    );
  }
}
