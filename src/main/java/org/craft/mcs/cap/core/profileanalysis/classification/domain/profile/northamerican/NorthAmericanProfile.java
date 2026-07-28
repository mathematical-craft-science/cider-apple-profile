package org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * North American cider-apple interpretation of sugar, acidity, and tannin levels.
 *
 * <p>Sugar and acidity are classification concepts derived from canonical SG and TA evidence.</p>
 */
public record NorthAmericanProfile(
    @NonNull NorthAmericanSugarLevel sugar,
    @NonNull NorthAmericanAcidityLevel acidity,
    @NonNull NorthAmericanTanninLevel tannin
) {

  public NorthAmericanProfile {
    requireNonNull(sugar);
    requireNonNull(acidity);
    requireNonNull(tannin);
  }
}
