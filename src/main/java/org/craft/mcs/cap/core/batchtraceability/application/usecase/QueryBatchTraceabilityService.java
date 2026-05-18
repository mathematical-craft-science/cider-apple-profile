package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import org.craft.mcs.cap.core.batchtraceability.application.port.in.QueryBatchTraceabilityUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceabilityGraph;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceabilityGraphProjector;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Application service for reading a material traceability graph.
 *
 * <p>The persistence query returns append-only steps where the requested subject is an input. This
 * service turns those immutable facts into a source tree projection, so old trace steps never need
 * to be mutated when more production batches are created from the same feedstock.
 */
public final class QueryBatchTraceabilityService implements QueryBatchTraceabilityUseCase {

  private final @NonNull BatchTraceabilityQueryStore queryStore;

  public QueryBatchTraceabilityService(@NonNull BatchTraceabilityQueryStore queryStore) {
    this.queryStore = requireNonNull(queryStore, "batch traceability query store cannot be null");
  }

  @Override
  public @NonNull BatchTraceabilityGraph graphFor(@NonNull BatchTraceSubject subject) {
    final var steps = queryStore.findByInputSubject(
        requireNonNull(subject, "batch trace subject cannot be null")
    );
    return BatchTraceabilityGraphProjector.sourceTreeFrom(steps);
  }
}
