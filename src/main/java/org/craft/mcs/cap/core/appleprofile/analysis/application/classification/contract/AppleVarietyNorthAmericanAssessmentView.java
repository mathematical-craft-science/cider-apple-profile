package org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.Nullable;


/**
 * Independently available North American interpretations for an apple-variety reference profile.
 *
 * <p>Unlike the complete domain profile, this application projection preserves useful
 * per-characteristic assessments when other reference characteristics are absent.</p>
 */
public record AppleVarietyNorthAmericanAssessmentView(
    @Nullable SugarLevel sugar,
    @Nullable AcidityLevel acidity,
    @Nullable TanninLevel tannin
) {

  public AppleVarietyNorthAmericanAssessmentView {
    if (sugar == null && acidity == null && tannin == null) {
      throw new ApplicationValidationException("North American assessment cannot be empty");
    }
  }

  public enum SugarLevel { LOW, MEDIUM, HIGH, VERY_HIGH }

  public enum AcidityLevel { LOW, MEDIUM, HIGH, VERY_HIGH }

  public enum TanninLevel { LOW, MEDIUM, HIGH }
}
