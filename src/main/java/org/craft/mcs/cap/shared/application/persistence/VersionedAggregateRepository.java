package org.craft.mcs.cap.shared.application.persistence;

import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Application repository contract that separates aggregate creation from revision-checked update.
 *
 * <p>The separate operations prevent accidental upsert semantics and make stale-write protection
 * mandatory for every mutation of an existing aggregate.
 */
public interface VersionedAggregateRepository<
    A extends @NonNull Object,
    I extends @NonNull Object
> {

  @NonNull A create(@NonNull A aggregate);

  @NonNull VersionedAggregate<A> update(@NonNull VersionedAggregate<A> aggregate);

  @NonNull Optional<@NonNull VersionedAggregate<A>> findVersionedById(@NonNull I aggregateId);

  default @NonNull Optional<@NonNull A> findById(@NonNull I aggregateId) {
    return findVersionedById(aggregateId).map(VersionedAggregate::aggregate);
  }
}
