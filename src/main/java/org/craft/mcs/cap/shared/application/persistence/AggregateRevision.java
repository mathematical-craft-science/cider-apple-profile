package org.craft.mcs.cap.shared.application.persistence;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

/**
 * Application-level concurrency token identifying the persisted revision of an aggregate.
 *
 * <p>The token deliberately does not expose a persistence framework. Write use cases retain it
 * from aggregate loading through update so outbound adapters can reject stale state.
 */
public record AggregateRevision(long value) {

  public AggregateRevision {
    if (value < 0) {
      throw new ApplicationValidationException("aggregate revision cannot be negative");
    }
  }

  public static @NonNull AggregateRevision of(long value) {
    return new AggregateRevision(value);
  }
}
