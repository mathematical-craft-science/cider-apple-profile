package org.craft.mcs.cap.core.traceability.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


public interface BatchTraceabilityQueryStore {

  /** Loads all immutable trace facts for rebuilding workspace-owned projections. */
  @NonNull
  List<@NonNull BatchTraceStep> findAllByWorkspace(@NonNull WorkspaceId workspaceId);

  /** Resolves the workspace owning trace facts for a globally identified material subject. */
  @NonNull
  Optional<@NonNull WorkspaceId> findWorkspaceIdBySubject(@NonNull BatchTraceSubject subject);

  /** Checks for any trace participation other than the subject's own creation output. */
  boolean hasOperationStep(
      @NonNull BatchTraceSubjectType subjectType,
      @NonNull UUID subjectId,
      @NonNull BatchTraceStepType creationStepType
  );

  /**
   * Finds immutable trace steps where the requested subject participates as a material input.
   *
   * <p>This is the persistence-facing contract for source tree projection: a single source batch can
   * produce several production batches over time, so persistence returns many append-only facts
   * instead of a pre-mutated graph.
   */
  @NonNull
  List<@NonNull BatchTraceStep> findByInputSubject(
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchTraceSubject subject
  );

  /**
   * Finds immutable trace steps where the requested subject participates as a material output.
   *
   * <p>This is used by source/origin projections for a production batch: the production batch is
   * the output, while source feedstock batches or composition results are inputs.
   */
  @NonNull
  List<@NonNull BatchTraceStep> findByOutputSubject(
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchTraceSubject subject
  );
}
