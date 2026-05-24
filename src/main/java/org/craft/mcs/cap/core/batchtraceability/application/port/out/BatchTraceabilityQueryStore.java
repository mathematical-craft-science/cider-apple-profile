package org.craft.mcs.cap.core.batchtraceability.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.jspecify.annotations.NonNull;


public interface BatchTraceabilityQueryStore {

  /**
   * Finds immutable trace steps where the requested subject participates as a material input.
   *
   * <p>This is the persistence-facing contract for source tree projection: a single source batch can
   * produce several production batches over time, so persistence returns many append-only facts
   * instead of a pre-mutated graph.
   */
  @NonNull
  List<@NonNull BatchTraceStep> findByInputSubject(@NonNull BatchTraceSubject subject);

  /**
   * Finds immutable trace steps where the requested subject participates as a material output.
   *
   * <p>This is used by source/origin projections for a production batch: the production batch is
   * the output, while source feedstock batches or composition results are inputs.
   */
  @NonNull
  List<@NonNull BatchTraceStep> findByOutputSubject(@NonNull BatchTraceSubject subject);
}
