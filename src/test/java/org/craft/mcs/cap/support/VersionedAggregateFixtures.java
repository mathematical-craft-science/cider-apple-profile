package org.craft.mcs.cap.support;

import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;

/** Creates deterministic persisted aggregate envelopes for application tests. */
public final class VersionedAggregateFixtures {

  private static final @NonNull AggregateRevision REVISION = AggregateRevision.of(1);

  private VersionedAggregateFixtures() {
  }

  public static <T extends @NonNull Object> @NonNull VersionedAggregate<T> versioned(
      @NonNull T aggregate
  ) {
    return new VersionedAggregate<>(aggregate, REVISION);
  }
}
