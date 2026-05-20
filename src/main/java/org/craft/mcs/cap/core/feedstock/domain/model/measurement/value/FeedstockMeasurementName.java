package org.craft.mcs.cap.core.feedstock.domain.model.measurement.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record FeedstockMeasurementName(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public FeedstockMeasurementName {
    requireNonNull(value, "feedstock measurement name cannot be null");

    value = value.strip();

    if (value.isBlank()) {
      throw new DomainRuleViolationException("feedstock measurement name cannot be blank");
    }

    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "feedstock measurement name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull FeedstockMeasurementName of(@NonNull String value) {
    return new FeedstockMeasurementName(value);
  }
}
