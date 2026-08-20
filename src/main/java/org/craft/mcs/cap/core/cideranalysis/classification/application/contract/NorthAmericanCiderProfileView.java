package org.craft.mcs.cap.core.cideranalysis.classification.application.contract;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.Nullable;


/** Independently available North American sugar, acidity, and tannin interpretations. */
public record NorthAmericanCiderProfileView(
    @Nullable SugarLevel sugar,
    @Nullable AcidityLevel acidity,
    @Nullable TanninLevel tannin
) {

  public NorthAmericanCiderProfileView {
    if (sugar == null && acidity == null && tannin == null) {
      throw new ApplicationValidationException("North American cider profile cannot be empty");
    }
  }

  public enum SugarLevel { LOW, MEDIUM, HIGH, VERY_HIGH }

  public enum AcidityLevel { LOW, MEDIUM, HIGH, VERY_HIGH }

  public enum TanninLevel { LOW, MEDIUM, HIGH }
}
