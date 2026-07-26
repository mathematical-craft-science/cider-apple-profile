package org.craft.mcs.cap.core.traceability.application.usecase;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.traceability.application.BatchTraceSubjectNotFoundException;
import org.craft.mcs.cap.core.traceability.application.port.in.QueryBatchTraceabilityUseCase;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.domain.graph.BatchTraceabilityGraph;
import org.craft.mcs.cap.core.traceability.domain.graph.BatchTraceabilityGraphProjector;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
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
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public QueryBatchTraceabilityService(
      @NonNull BatchTraceabilityQueryStore queryStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.queryStore = requireNonNull(queryStore, "batch traceability query store cannot be null");
    this.accessControl = requireNonNull(accessControl, "batch traceability access control cannot be null");
  }

  @Override
  public @NonNull BatchTraceabilityGraph graphFor(@NonNull BatchTraceSubject subject) {
    final var workspaceId = workspaceIdFor(subject);
    final var steps = queryStore.findByInputSubject(
        workspaceId,
        subject
    );
    return BatchTraceabilityGraphProjector.sourceTreeFrom(steps);
  }

  @Override
  public @NonNull BatchTraceabilityGraph sourceGraphFor(@NonNull BatchTraceSubject subject) {
    final var workspaceId = workspaceIdFor(subject);
    final var stepsById = new LinkedHashMap<BatchTraceStepId, BatchTraceStep>();
    collectSourceSteps(
        workspaceId,
        requireNonNull(subject, "batch trace subject cannot be null"),
        new HashSet<>(),
        stepsById
    );

    final var steps = List.copyOf(stepsById.values());
    return BatchTraceabilityGraphProjector.sourceTreeFrom(steps);
  }

  private @NonNull WorkspaceId workspaceIdFor(@NonNull BatchTraceSubject subject) {
    requireNonNull(subject, "batch trace subject cannot be null");
    final var workspaceId = queryStore.findWorkspaceIdBySubject(subject)
        .orElseThrow(() -> new BatchTraceSubjectNotFoundException(subject));
    accessControl.requireReadAccess(workspaceId);
    return workspaceId;
  }

  private void collectSourceSteps(
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchTraceSubject subject,
      @NonNull Set<@NonNull BatchTraceSubject> visitedSubjects,
      @NonNull LinkedHashMap<@NonNull BatchTraceStepId, @NonNull BatchTraceStep> stepsById
  ) {
    if (!visitedSubjects.add(subject)) {
      return;
    }

    for (final var step : queryStore.findByOutputSubject(workspaceId, subject)) {
      stepsById.putIfAbsent(step.stepId(), step);
      for (final var input : step.inputs()) {
        collectSourceSteps(workspaceId, input.subject(), visitedSubjects, stepsById);
      }
    }
  }
}
