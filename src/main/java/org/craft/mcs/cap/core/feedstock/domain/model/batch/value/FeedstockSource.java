package org.craft.mcs.cap.core.feedstock.domain.model.batch.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Human-readable origin reference supplied for a FeedstockBatch.
 *
 * <p>Structured provenance and regional source profiles belong to their dedicated contexts. This
 * value preserves the concise operational source description used by the current Feedstock model.</p>
 */
@ValueObject
public record FeedstockSource(@NonNull String value) {

  private static final int MAX_LENGTH = 255;

  public FeedstockSource {
    requireNonNull(value, "feedstock source cannot be null");

    value = value.strip();

    if (value.isBlank()) {
      throw new DomainRuleViolationException("feedstock source cannot be blank");
    }

    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "feedstock source length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull FeedstockSource of(@NonNull String value) {
    return new FeedstockSource(value);
  }
}
