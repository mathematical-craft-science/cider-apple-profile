package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.traceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateHistoryStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.id;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcBatchTraceabilityStore.class,
    JdbcBatchTraceabilityQueryStore.class,
    JdbcMaterialStateHistoryStore.class
})
class JdbcBatchTraceabilityPersistenceDataJdbcTest {

  @Autowired
  private BatchTraceabilityStore store;

  @Autowired
  private BatchTraceabilityQueryStore queryStore;

  @Autowired
  private MaterialStateHistoryStore historyStore;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final WorkspaceId workspaceId = WorkspaceId.newId();

  @BeforeEach
  void createWorkspace() {
    insertWorkspace(workspaceId, "Traceability test workspace");
  }

  @Test
  void persistsAndQueriesOneImmutableTransition() {
    final var firstInput = new BatchTraceStepSubject(
        MaterialStateId.newId(),
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"),
        BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("100"))
    );
    final var secondInput = new BatchTraceStepSubject(
        MaterialStateId.newId(),
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-2"),
        BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("80"))
    );
    final var output = new BatchTraceStepSubject(
        MaterialStateId.newId(),
        subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"),
        BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("180")),
        Set.of(MeasurementCharacteristic.PH)
    );
    final var step = BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(firstInput, secondInput),
        List.of(output),
        Instant.parse("2026-05-18T00:00:00Z"),
        ActorReference.machineIdentity(id("test-actor")),
        null
    );

    store.append(step);

    assertThat(queryStore.findByInputSubject(workspaceId, firstInput.subject()))
        .singleElement()
        .returns(step.stepId(), BatchTraceStep::stepId)
        .satisfies(found -> assertThat(found.inputs())
            .extracting(BatchTraceStepSubject::materialStateId)
            .containsExactly(
                firstInput.materialStateId(),
                secondInput.materialStateId()
            ));
    assertThat(queryStore.findByOutputSubject(workspaceId, output.subject()))
        .singleElement()
        .returns(step.stepId(), BatchTraceStep::stepId)
        .satisfies(found -> assertThat(found.outputs()).singleElement()
            .satisfies(foundOutput -> assertThat(foundOutput.invalidatedCharacteristics())
                .containsExactly(MeasurementCharacteristic.PH)));
    assertThat(historyStore.find(output.materialStateId()))
        .get()
        .satisfies(state -> assertThat(state)
            .returns(workspaceId, MaterialStateSnapshot::workspaceId)
            .satisfies(snapshot -> assertThat(snapshot.state().snapshot().volume().liters())
                .isEqualByComparingTo("180")));
  }

  @Test
  void isolatesTraceQueriesByWorkspace() {
    final var otherWorkspaceId = WorkspaceId.newId();
    insertWorkspace(otherWorkspaceId, "Other traceability test workspace");
    final var sharedSubject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "shared-subject");
    final var firstStep = creationStep(workspaceId, sharedSubject, MaterialStateId.newId());
    final var otherStep = creationStep(otherWorkspaceId, sharedSubject, MaterialStateId.newId());

    store.append(firstStep);
    store.append(otherStep);

    assertThat(queryStore.findByOutputSubject(workspaceId, sharedSubject))
        .singleElement()
        .returns(firstStep.stepId(), BatchTraceStep::stepId)
        .returns(workspaceId, BatchTraceStep::workspaceId);
  }

  @Test
  void rejectsStateProducedByMoreThanOneStep() {
    final var sharedStateId = MaterialStateId.newId();
    store.append(creationStep(
        workspaceId,
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"),
        sharedStateId
    ));

    final var duplicate = creationStep(
        workspaceId,
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-2"),
        sharedStateId
    );

    assertThatThrownBy(() -> store.append(duplicate))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessageContaining("material state");
  }

  private void insertWorkspace(
      WorkspaceId id,
      String name
  ) {
    final var now = OffsetDateTime.parse("2026-05-18T00:00:00Z");
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
        id.value(),
        name,
        "CIDERY",
        "ACTIVE",
        0L,
        now,
        now
    );
  }

  private static BatchTraceStep creationStep(
      WorkspaceId workspaceId,
      BatchTraceSubject subject,
      MaterialStateId stateId
  ) {
    return BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(new BatchTraceStepSubject(
            stateId,
            subject,
            BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("100"))
        )),
        Instant.parse("2026-05-18T00:00:00Z"),
        ActorReference.machineIdentity(id("test-actor")),
        null
    );
  }
}
