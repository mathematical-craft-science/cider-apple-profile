package org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.out;

import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassification;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView;
import org.jspecify.annotations.NonNull;


/** AppleProfile-owned outbound port for cider-apple interpretations. */
public interface AppleCiderClassificationAnalyzer {

  @NonNull
  AppleVarietyClassification classify(
      @NonNull AppleVarietyAnalysisProfile profile,
      @NonNull AppleVarietyClassificationSystem system
  );

  @NonNull
  AppleVarietyNorthAmericanAssessmentView assessNorthAmerican(
      @NonNull AppleVarietyAnalysisProfile profile
  );
}
