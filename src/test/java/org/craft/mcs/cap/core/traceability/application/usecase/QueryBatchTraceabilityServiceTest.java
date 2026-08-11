package org.craft.mcs.cap.core.traceability.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.id;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;


class QueryBatchTraceabilityServiceTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.newId();

  @Test
  void queriesStepsByInputSubjectAndProjectsGraph() {
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var step = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(state(feedstock)),
        List.of(state(subject(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            "production-1"
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ActorReference.machineIdentity(id("test-actor")),
        null
    );
    final var queryStore = new StubQueryStore(List.of(step));
    final var accessControl = new CapturingAccessControl();
    final var service = new QueryBatchTraceabilityService(queryStore, accessControl);

    final var graph = service.graphFor(feedstock);

    assertThat(queryStore)
        .returns(WORKSPACE_ID, StubQueryStore::requestedWorkspaceId)
        .returns(feedstock, StubQueryStore::requestedSubject);
    assertThat(accessControl.requestedWorkspaceId).isEqualTo(WORKSPACE_ID);
    assertThat(graph.nodes()).hasSize(2);
    assertThat(graph.edges()).hasSize(1);
  }

  @Test
  void queriesStepsByOutputSubjectAndProjectsSourceGraph() {
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var step = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(state(subject(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(state(production)),
        Instant.parse("2026-05-18T00:00:00Z"),
        ActorReference.machineIdentity(id("test-actor")),
        null
    );
    final var queryStore = new StubQueryStore(List.of(step));
    final var accessControl = new CapturingAccessControl();
    final var service = new QueryBatchTraceabilityService(queryStore, accessControl);

    final var graph = service.sourceGraphFor(production);

    assertThat(queryStore.requestedOutputSubjects)
        .containsExactly(
            production,
            step.inputs().getFirst().subject()
        );
    assertThat(accessControl.requestedWorkspaceId).isEqualTo(WORKSPACE_ID);
    assertThat(graph.nodes()).hasSize(2);
    assertThat(graph.edges()).hasSize(1);
  }

  private static BatchTraceStepSubject state(BatchTraceSubject subject) {
    return BatchTraceStepSubject.reference(MaterialStateId.newId(), subject);
  }

  private static final class StubQueryStore implements BatchTraceabilityQueryStore {

    private final List<BatchTraceStep> steps;
    private WorkspaceId requestedWorkspaceId;
    private BatchTraceSubject requestedSubject;
    private final List<BatchTraceSubject> requestedOutputSubjects = new ArrayList<>();

    private StubQueryStore(List<BatchTraceStep> steps) {
      this.steps = List.copyOf(steps);
    }

    @Override
    public @NonNull List<@NonNull BatchTraceStep> findAllByWorkspace(
        @NonNull WorkspaceId workspaceId
    ) {
      requestedWorkspaceId = workspaceId;
      return steps;
    }

    @Override
    public @NonNull Optional<@NonNull WorkspaceId> findWorkspaceIdBySubject(
        @NonNull BatchTraceSubject subject
    ) {
      requestedSubject = subject;
      return Optional.of(WORKSPACE_ID);
    }

    @Override
    public boolean hasOperationStep(
        @NonNull BatchTraceSubjectType subjectType,
        @NonNull UUID subjectId,
        @NonNull BatchTraceStepType creationStepType
    ) {
      return steps.stream().anyMatch(step -> step.stepType() != creationStepType);
    }

    @Override
    public @NonNull List<@NonNull BatchTraceStep> findByInputSubject(
        @NonNull WorkspaceId workspaceId,
        @NonNull BatchTraceSubject subject
    ) {
      requestedWorkspaceId = workspaceId;
      requestedSubject = subject;
      return steps;
    }

    @Override
    public @NonNull List<@NonNull BatchTraceStep> findByOutputSubject(
        @NonNull WorkspaceId workspaceId,
        @NonNull BatchTraceSubject subject
    ) {
      requestedWorkspaceId = workspaceId;
      requestedOutputSubjects.add(subject);
      return steps.stream()
          .filter(step -> step.outputs().stream()
              .map(BatchTraceStepSubject::subject)
              .anyMatch(subject::equals))
          .toList();
    }

    private WorkspaceId requestedWorkspaceId() {
      return requestedWorkspaceId;
    }

    private BatchTraceSubject requestedSubject() {
      return requestedSubject;
    }
  }

  private static final class CapturingAccessControl implements BatchTraceabilityAccessControl {

    private WorkspaceId requestedWorkspaceId;

    @Override
    public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
      requestedWorkspaceId = workspaceId;
    }

    @Override
    public void requireWriteAccess(
        @NonNull WorkspaceId workspaceId,
        @NonNull Set<@NonNull BatchTraceSubjectType> subjectTypes
    ) {
      requestedWorkspaceId = workspaceId;
    }
  }
}
