package org.craft.mcs.cap.core.feedstock.domain.model.value;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record FeedstockSampleName(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public FeedstockSampleName {
    requireNonNull(value, "feedstock sample name cannot be null");

    value = value.strip();

    if (value.isBlank()) {
      throw new IllegalArgumentException("feedstock sample name cannot be blank");
    }

    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "feedstock sample name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull FeedstockSampleName of(@NonNull String value) {
    return new FeedstockSampleName(value);
  }
}
