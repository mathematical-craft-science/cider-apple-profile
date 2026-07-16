package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Feedstock-owned name snapshot exposed through the material-consumption application boundary. */
public record FeedstockBatchMaterialName(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public FeedstockBatchMaterialName {
    value = requireNonNull(value, "feedstock batch material name cannot be null").strip();

    if (value.isEmpty()) {
      throw new ApplicationValidationException("feedstock batch material name cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new ApplicationValidationException(
          "feedstock batch material name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull FeedstockBatchMaterialName of(@NonNull String value) {
    return new FeedstockBatchMaterialName(value);
  }
}
