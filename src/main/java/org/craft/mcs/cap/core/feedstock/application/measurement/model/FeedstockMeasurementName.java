package org.craft.mcs.cap.core.feedstock.application.measurement.model;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Compatibility label projected from the current generic Measurement Set revision.
 *
 * <p>This type belongs to the Feedstock application boundary and is not a Feedstock domain value object.
 */
public record FeedstockMeasurementName(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public FeedstockMeasurementName {
    requireNonNull(value, "feedstock measurement name cannot be null");

    value = value.strip();

    if (value.isBlank()) {
      throw new ApplicationValidationException("feedstock measurement name cannot be blank");
    }

    if (value.length() > MAX_LENGTH) {
      throw new ApplicationValidationException(
          "feedstock measurement name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull FeedstockMeasurementName of(@NonNull String value) {
    return new FeedstockMeasurementName(value);
  }
}
