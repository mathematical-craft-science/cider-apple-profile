package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanAcidityLevel;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanSugarLevel;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.Nullable;


/**
 * Independently available North American interpretations for an apple-variety reference profile.
 *
 * <p>Unlike the complete domain profile, this application projection preserves useful
 * per-characteristic assessments when other reference characteristics are absent.</p>
 */
public record AppleVarietyNorthAmericanAssessmentView(
    @Nullable NorthAmericanSugarLevel sugar,
    @Nullable NorthAmericanAcidityLevel acidity,
    @Nullable NorthAmericanTanninLevel tannin
) {

  public AppleVarietyNorthAmericanAssessmentView {
    if (sugar == null && acidity == null && tannin == null) {
      throw new ApplicationValidationException("North American assessment cannot be empty");
    }
  }
}
