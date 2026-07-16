package org.craft.mcs.cap.shared.application.persistence;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * Carries an aggregate together with the revision observed when it was loaded.
 *
 * <p>Revision is application concurrency metadata and therefore remains outside the domain model.
 * Replacing the aggregate preserves the observed revision until a successful persistence update
 * returns a new envelope.
 */
public record VersionedAggregate<T extends @NonNull Object>(
    @NonNull T aggregate,
    @NonNull AggregateRevision revision
) {

  public VersionedAggregate {
    requireNonNull(aggregate, "aggregate cannot be null");
    requireNonNull(revision, "aggregate revision cannot be null");
  }

  public @NonNull VersionedAggregate<T> withAggregate(@NonNull T updatedAggregate) {
    return new VersionedAggregate<>(
        requireNonNull(updatedAggregate, "updated aggregate cannot be null"),
        revision
    );
  }
}
