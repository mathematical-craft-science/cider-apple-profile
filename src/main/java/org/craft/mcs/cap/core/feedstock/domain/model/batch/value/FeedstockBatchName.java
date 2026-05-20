package org.craft.mcs.cap.core.feedstock.domain.model.batch.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record FeedstockBatchName(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public FeedstockBatchName {
    requireNonNull(value, "feedstock batch name cannot be null");

    value = value.strip();

    if (value.isBlank()) {
      throw new DomainRuleViolationException("feedstock batch name cannot be blank");
    }

    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "feedstock batch name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull FeedstockBatchName of(@NonNull String value) {
    return new FeedstockBatchName(value);
  }
}
