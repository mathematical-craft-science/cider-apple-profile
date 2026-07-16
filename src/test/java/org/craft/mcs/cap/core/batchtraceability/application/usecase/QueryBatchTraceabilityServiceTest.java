package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class QueryBatchTraceabilityServiceTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.newId();

  @Test
  void queriesStepsByInputSubjectAndProjectsGraph() {
    final var feedstock = BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var step = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(feedstock)),
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            "production-1"
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
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
    final var production = BatchTraceSubject.of(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var step = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(BatchTraceStepSubject.of(production)),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
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

  private static final class StubQueryStore implements BatchTraceabilityQueryStore {

    private final List<BatchTraceStep> steps;
    private WorkspaceId requestedWorkspaceId;
    private BatchTraceSubject requestedSubject;
    private final List<BatchTraceSubject> requestedOutputSubjects = new ArrayList<>();

    private StubQueryStore(List<BatchTraceStep> steps) {
      this.steps = List.copyOf(steps);
    }

    @Override
    public @NonNull Optional<@NonNull WorkspaceId> findWorkspaceIdBySubject(
        @NonNull BatchTraceSubject subject
    ) {
      requestedSubject = subject;
      return Optional.of(WORKSPACE_ID);
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
  }
}
