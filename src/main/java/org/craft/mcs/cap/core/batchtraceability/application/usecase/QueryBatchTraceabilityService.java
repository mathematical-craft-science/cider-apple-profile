package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.QueryBatchTraceabilityUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceabilityGraph;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceabilityGraphProjector;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Application service for reading a material traceability graph.
 *
 * <p>The service turns immutable trace facts into graph projections. Downstream queries start from
 * a material input and show what it produced. Source queries start from a material output, such as a
 * production batch, and show the trace step that created it from its inputs.
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

  @Override
  public @NonNull BatchTraceabilityGraph sourceGraphFor(@NonNull BatchTraceSubject subject) {
    final var stepsById = new LinkedHashMap<BatchTraceStepId, BatchTraceStep>();
    collectSourceSteps(
        requireNonNull(subject, "batch trace subject cannot be null"),
        new HashSet<>(),
        stepsById
    );

    final var steps = List.copyOf(stepsById.values());
    return BatchTraceabilityGraphProjector.sourceTreeFrom(steps);
  }

  private void collectSourceSteps(
      @NonNull BatchTraceSubject subject,
      @NonNull Set<@NonNull String> visitedSubjects,
      @NonNull LinkedHashMap<@NonNull BatchTraceStepId, @NonNull BatchTraceStep> stepsById
  ) {
    if (!visitedSubjects.add(subjectKey(subject))) {
      return;
    }

    for (final var step : queryStore.findByOutputSubject(subject)) {
      stepsById.putIfAbsent(step.stepId(), step);
      for (final var input : step.inputs()) {
        collectSourceSteps(input.subject(), visitedSubjects, stepsById);
      }
    }
  }

  private static @NonNull String subjectKey(@NonNull BatchTraceSubject subject) {
    return subject.type().name() + ':' + subject.id();
  }
}
