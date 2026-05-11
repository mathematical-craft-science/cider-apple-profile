package org.craft.mcs.cap.core.cideranalysis.classification.profile.northamerican;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record NorthAmericanProfile(
    @NonNull NorthAmericanLevel sugar,
    @NonNull NorthAmericanLevel acidity,
    @NonNull NorthAmericanLevel tannin
) {

  public NorthAmericanProfile {
    requireNonNull(sugar);
    requireNonNull(acidity);
    requireNonNull(tannin);
  }
}
