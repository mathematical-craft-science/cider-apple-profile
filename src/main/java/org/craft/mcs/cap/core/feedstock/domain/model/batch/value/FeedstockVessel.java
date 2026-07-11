package org.craft.mcs.cap.core.feedstock.domain.model.batch.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Human-readable vessel, tank or container where a feedstock batch is currently held.
 */
@ValueObject
public record FeedstockVessel(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public FeedstockVessel {
    requireNonNull(value, "feedstock vessel cannot be null");

    value = value.strip();

    if (value.isBlank()) {
      throw new DomainRuleViolationException("feedstock vessel cannot be blank");
    }

    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "feedstock vessel length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull FeedstockVessel of(@NonNull String value) {
    return new FeedstockVessel(value);
  }
}
