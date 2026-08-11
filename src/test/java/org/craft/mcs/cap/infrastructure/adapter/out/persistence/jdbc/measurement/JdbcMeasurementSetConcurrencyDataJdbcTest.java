package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.measurement.application.MeasurementEvidenceIdentityConflictException;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetAlreadyExistsException;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionChangeKind;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;


/** Verifies aggregate-local serialization and atomic rollback under real PostgreSQL transactions. */
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcMeasurementSetRepository.class,
    JdbcMeasurementSetAggregateReader.class,
    JdbcMeasurementSetAggregateWriter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JdbcMeasurementSetConcurrencyDataJdbcTest {

  private static final Instant RECORDED_AT = Instant.parse("2026-08-10T08:00:00Z");

  @Autowired
  private MeasurementSetRepository repository;

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  private PlatformTransactionManager transactionManager;

  private WorkspaceId workspaceId;

  @BeforeEach
  void createWorkspace() {
    workspaceId = WorkspaceId.newId();
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (
                workspace_id,
                name,
                type,
                status,
                version,
                created_at,
                updated_at
            ) VALUES (
                :workspaceId,
                'Measurement concurrency test workspace',
                'CIDERY',
                'ACTIVE',
                0,
                :createdAt,
                :createdAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("workspaceId", workspaceId.value())
            .addValue("createdAt", OffsetDateTime.parse("2026-08-10T08:00:00Z"))
    );
  }

  @AfterEach
  void deleteWorkspaceData() {
    final var parameters = new MapSqlParameterSource("workspaceId", workspaceId.value());
    jdbcTemplate.update(
        """
            DELETE FROM measurement_derivation_inputs
             WHERE measurement_set_id IN (
                 SELECT measurement_set_id
                   FROM measurement_sets
                  WHERE workspace_id = :workspaceId
             )
            """,
        parameters
    );
    jdbcTemplate.update(
        """
            DELETE FROM measurement_revision_records
             WHERE measurement_set_id IN (
                 SELECT measurement_set_id
                   FROM measurement_sets
                  WHERE workspace_id = :workspaceId
             )
            """,
        parameters
    );
    jdbcTemplate.update(
        """
            DELETE FROM measurement_records
             WHERE measurement_set_id IN (
                 SELECT measurement_set_id
                   FROM measurement_sets
                  WHERE workspace_id = :workspaceId
             )
            """,
        parameters
    );
    jdbcTemplate.update(
        """
            DELETE FROM measurement_set_heads
             WHERE measurement_set_id IN (
                 SELECT measurement_set_id
                   FROM measurement_sets
                  WHERE workspace_id = :workspaceId
             )
            """,
        parameters
    );
    jdbcTemplate.update(
        """
            DELETE FROM measurement_set_revisions
             WHERE measurement_set_id IN (
                 SELECT measurement_set_id
                   FROM measurement_sets
                  WHERE workspace_id = :workspaceId
             )
            """,
        parameters
    );
    jdbcTemplate.update(
        "DELETE FROM measurement_sets WHERE workspace_id = :workspaceId",
        parameters
    );
    jdbcTemplate.update(
        "DELETE FROM workspaces WHERE workspace_id = :workspaceId",
        parameters
    );
  }

  @RepeatedTest(10)
  void concurrentCorrectionsAllowExactlyOneRevisionToAdvanceTheHead() throws Exception {
    final var initial = createMeasurementSet();
    final var first = corrected(copy(initial), "3.35", "First concurrent correction");
    final var second = corrected(copy(initial), "3.30", "Second concurrent correction");

    final var outcomes = appendConcurrently(first, second);

    assertThat(outcomes)
        .containsExactlyInAnyOrder(WriteOutcome.SUCCESS, WriteOutcome.CONFLICT);
    assertSingleWinningRevision(initial.aggregate().measurementSetId(), first, second);
  }

  @RepeatedTest(10)
  void concurrentCorrectionAndRetractionAllowExactlyOneLifecycleTransition() throws Exception {
    final var initial = createMeasurementSet();
    final var correction = corrected(copy(initial), "3.35", "Concurrent correction");
    final var retraction = retracted(copy(initial));

    final var outcomes = appendConcurrently(correction, retraction);

    assertThat(outcomes)
        .containsExactlyInAnyOrder(WriteOutcome.SUCCESS, WriteOutcome.CONFLICT);
    assertSingleWinningRevision(initial.aggregate().measurementSetId(), correction, retraction);
  }

  @RepeatedTest(10)
  void concurrentCorrectionsOfIndependentSetsBothSucceed() throws Exception {
    final var firstInitial = createMeasurementSet();
    final var secondInitial = createMeasurementSet();
    final var first = corrected(copy(firstInitial), "3.35", "First independent correction");
    final var second = corrected(copy(secondInitial), "3.30", "Second independent correction");

    final var outcomes = appendConcurrently(first, second);

    assertThat(outcomes).containsOnly(WriteOutcome.SUCCESS);
    assertPersistedRevision(firstInitial.aggregate().measurementSetId(), first);
    assertPersistedRevision(secondInitial.aggregate().measurementSetId(), second);
  }

  @RepeatedTest(10)
  void concurrentCreationWithTheSameRequestedIdentityCommitsExactlyOneAggregate() throws Exception {
    final var measurementSetId = MeasurementSetId.newId();
    final var first = measurementSetWith(measurementSetId, result("3.40"));
    final var second = measurementSetWith(measurementSetId, result("3.35"));

    final var outcomes = createConcurrently(first, second);

    assertThat(outcomes)
        .containsExactlyInAnyOrder(WriteOutcome.SUCCESS, WriteOutcome.CONFLICT);
    final var persisted = inTransaction(() -> repository.findVersionedById(measurementSetId)
        .orElseThrow());
    assertThat(persisted)
        .returns(0L, state -> state.revision().value())
        .satisfies(state -> assertThat(state.aggregate().currentRevision().revisionId())
            .isIn(
                first.currentRevision().revisionId(),
                second.currentRevision().revisionId()
            ));
    assertThat(revisionCount(measurementSetId)).isEqualTo(1);
  }

  @Test
  void appendCannotSkipAnImmutableRevisionEvenWhenTheAggregateVersionMatches() {
    final var initial = createMeasurementSet();
    final var skipped = corrected(
        corrected(copy(initial), "3.35", "Unpersisted intermediate correction"),
        "3.30",
        "Attempted skipped correction"
    );

    assertThatThrownBy(() -> inTransaction(() -> repository.appendRevision(skipped)))
        .isInstanceOf(ConcurrentResourceModificationException.class);
    final var persisted = inTransaction(() -> repository.findVersionedById(
        initial.aggregate().measurementSetId()
    ).orElseThrow());
    assertThat(persisted)
        .returns(0L, state -> state.revision().value())
        .satisfies(state -> assertThat(state.aggregate().currentRevision().revisionId())
            .isEqualTo(initial.aggregate().currentRevision().revisionId()));
    assertThat(revisionCount(initial.aggregate().measurementSetId())).isEqualTo(1);
  }

  @Test
  void lateEvidenceConflictRollsBackVersionRevisionHeadAndEarlierRecordInserts() {
    final var foreign = inTransaction(() -> repository.create(newMeasurementSet(
        MeasurementCharacteristic.TANNIN,
        "1200"
    )));
    final var target = createMeasurementSet();
    final var corrected = copy(target);
    final var targetAggregate = corrected.aggregate();
    final var current = targetAggregate.currentRevision();
    final var newPhResult = result("3.35");
    final var foreignTanninResult = foreign.aggregate().currentRevision().records().getFirst();
    targetAggregate.correct(new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        current.revisionNumber().next(),
        current.materialStateId(),
        current.name(),
        current.note(),
        MeasurementCorrectionReason.of("Correction containing foreign evidence identity"),
        current.createdAt().plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        List.of(newPhResult, foreignTanninResult),
        List.of()
    ));

    assertThatThrownBy(() -> inTransaction(() -> repository.appendRevision(corrected)))
        .isInstanceOf(MeasurementEvidenceIdentityConflictException.class);
    final var persisted = inTransaction(() -> repository.findVersionedById(
        target.aggregate().measurementSetId()
    ).orElseThrow());
    assertThat(persisted)
        .returns(0L, state -> state.revision().value())
        .satisfies(state -> assertThat(state.aggregate().currentRevision().revisionId())
            .isEqualTo(target.aggregate().currentRevision().revisionId()));
    assertThat(revisionCount(target.aggregate().measurementSetId())).isEqualTo(1);
    assertThat(recordExists(newPhResult.recordId())).isFalse();
  }

  @Test
  void evidenceConflictRollsBackCompleteMeasurementSetCreation() {
    final var foreign = inTransaction(() -> repository.create(newMeasurementSet(
        MeasurementCharacteristic.TANNIN,
        "1200"
    )));
    final var foreignResult = foreign.aggregate().currentRevision().records().getFirst();
    final var failedSet = measurementSetWith(foreignResult);

    assertThatThrownBy(() -> inTransaction(() -> repository.create(failedSet)))
        .isInstanceOf(MeasurementEvidenceIdentityConflictException.class);
    assertThat(measurementSetExists(failedSet.measurementSetId())).isFalse();
    assertThat(revisionCount(failedSet.measurementSetId())).isZero();
  }

  @Test
  void readerSeesCompleteOldOrNewRevisionButNeverAnUncommittedPartialAppend() throws Exception {
    final var initial = createMeasurementSet();
    final var corrected = corrected(copy(initial), "3.35", "Visibility correction");
    final var appendCompleted = new CountDownLatch(1);
    final var commitAllowed = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var writer = executor.submit(() -> inTransaction(() -> {
        repository.appendRevision(corrected);
        appendCompleted.countDown();
        await(commitAllowed);
        return WriteOutcome.SUCCESS;
      }));
      await(appendCompleted);
      try {
        final var duringAppend = inTransaction(() -> repository.findVersionedById(
            initial.aggregate().measurementSetId()
        ).orElseThrow());
        assertThat(duringAppend)
            .returns(0L, state -> state.revision().value())
            .satisfies(state -> assertThat(state.aggregate().currentRevision().revisionId())
                .isEqualTo(initial.aggregate().currentRevision().revisionId()));
      } finally {
        commitAllowed.countDown();
      }
      assertThat(writer.get()).isEqualTo(WriteOutcome.SUCCESS);
    }

    assertPersistedRevision(initial.aggregate().measurementSetId(), corrected);
  }

  private @NonNull VersionedAggregate<MeasurementSet> createMeasurementSet() {
    final var measurementSet = newMeasurementSet();
    return inTransaction(() -> repository.create(measurementSet));
  }

  private @NonNull List<WriteOutcome> appendConcurrently(
      @NonNull VersionedAggregate<MeasurementSet> first,
      @NonNull VersionedAggregate<MeasurementSet> second
  ) throws Exception {
    final var barrier = new CyclicBarrier(2);
    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var firstResult = executor.submit(() -> append(first, barrier));
      final var secondResult = executor.submit(() -> append(second, barrier));
      return List.of(firstResult.get(), secondResult.get());
    }
  }

  private @NonNull List<WriteOutcome> createConcurrently(
      @NonNull MeasurementSet first,
      @NonNull MeasurementSet second
  ) throws Exception {
    final var barrier = new CyclicBarrier(2);
    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var firstResult = executor.submit(() -> create(first, barrier));
      final var secondResult = executor.submit(() -> create(second, barrier));
      return List.of(firstResult.get(), secondResult.get());
    }
  }

  private @NonNull WriteOutcome create(
      @NonNull MeasurementSet measurementSet,
      @NonNull CyclicBarrier barrier
  ) {
    try {
      return inTransaction(() -> {
        await(barrier);
        repository.create(measurementSet);
        return WriteOutcome.SUCCESS;
      });
    } catch (MeasurementSetAlreadyExistsException exception) {
      return WriteOutcome.CONFLICT;
    }
  }

  private @NonNull WriteOutcome append(
      @NonNull VersionedAggregate<MeasurementSet> state,
      @NonNull CyclicBarrier barrier
  ) {
    try {
      return inTransaction(() -> {
        await(barrier);
        repository.appendRevision(state);
        return WriteOutcome.SUCCESS;
      });
    } catch (ConcurrentResourceModificationException exception) {
      return WriteOutcome.CONFLICT;
    }
  }

  private void assertSingleWinningRevision(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull VersionedAggregate<MeasurementSet> first,
      @NonNull VersionedAggregate<MeasurementSet> second
  ) {
    final var persisted = inTransaction(() -> repository.findVersionedById(measurementSetId)
        .orElseThrow());
    assertThat(persisted)
        .returns(1L, state -> state.revision().value())
        .satisfies(state -> assertThat(state.aggregate().currentRevision().revisionId())
            .isIn(
                first.aggregate().currentRevision().revisionId(),
                second.aggregate().currentRevision().revisionId()
            ));
    assertThat(revisionCount(measurementSetId)).isEqualTo(2);
  }

  private void assertPersistedRevision(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull VersionedAggregate<MeasurementSet> expected
  ) {
    final var persisted = inTransaction(() -> repository.findVersionedById(measurementSetId)
        .orElseThrow());
    assertThat(persisted)
        .returns(1L, state -> state.revision().value())
        .satisfies(state -> assertThat(state.aggregate().currentRevision().revisionId())
            .isEqualTo(expected.aggregate().currentRevision().revisionId()));
    assertThat(revisionCount(measurementSetId)).isEqualTo(2);
  }

  private @NonNull VersionedAggregate<MeasurementSet> copy(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    final var aggregate = state.aggregate();
    return new VersionedAggregate<>(
        MeasurementSet.restore(
            aggregate.measurementSetId(),
            aggregate.workspaceId(),
            aggregate.subject(),
            aggregate.currentRevision(),
            aggregate.createdAt()
        ),
        state.revision()
    );
  }

  private static @NonNull VersionedAggregate<MeasurementSet> corrected(
      @NonNull VersionedAggregate<MeasurementSet> state,
      @NonNull String ph,
      @NonNull String reason
  ) {
    final var aggregate = state.aggregate();
    final var current = aggregate.currentRevision();
    aggregate.correct(new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        current.revisionNumber().next(),
        current.materialStateId(),
        current.name(),
        current.note(),
        MeasurementCorrectionReason.of(reason),
        current.createdAt().plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        List.of(result(ph)),
        List.of()
    ));
    return state;
  }

  private static @NonNull VersionedAggregate<MeasurementSet> retracted(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    final var aggregate = state.aggregate();
    final var current = aggregate.currentRevision();
    aggregate.retract(new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        current.revisionNumber().next(),
        current.materialStateId(),
        MeasurementSetLifecycle.RETRACTED,
        MeasurementSetRevisionChangeKind.RETRACTION,
        current.name(),
        current.note(),
        null,
        MeasurementRetractionReason.of("Concurrent full retraction"),
        current.createdAt().plusSeconds(60),
        ActorReference.machineIdentity(newUuid()),
        current.records(),
        current.resultRetractions()
    ));
    return state;
  }

  private @NonNull MeasurementSet newMeasurementSet() {
    return newMeasurementSet(MeasurementCharacteristic.PH, "3.40");
  }

  private @NonNull MeasurementSet newMeasurementSet(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull String numericValue
  ) {
    return measurementSetWith(result(characteristic, numericValue));
  }

  private @NonNull MeasurementSet measurementSetWith(@NonNull MeasurementRecord measurementRecord) {
    return measurementSetWith(MeasurementSetId.newId(), measurementRecord);
  }

  private @NonNull MeasurementSet measurementSetWith(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementRecord measurementRecord
  ) {
    final var materialStateId = MaterialStateId.newId();
    final var revision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.initial(),
        materialStateId,
        MeasurementSetName.of("Concurrent measurement"),
        null,
        null,
        RECORDED_AT,
        ActorReference.accountUser(newUuid()),
        List.of(measurementRecord),
        List.of()
    );
    return MeasurementSet.create(
        measurementSetId,
        workspaceId,
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        revision
    );
  }

  private static @NonNull MeasurementRecord result(@NonNull String ph) {
    return result(MeasurementCharacteristic.PH, ph);
  }

  private static @NonNull MeasurementRecord result(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull String numericValue
  ) {
    final PointCharacteristicValue<MeasuredValue> measuredValue = PointCharacteristicValue.of(
        value(characteristic, new BigDecimal(numericValue))
    );
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        characteristic,
        MeasurementRecordRole.RESULT,
        measuredValue,
        RECORDED_AT,
        new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null),
        null,
        List.of()
    );
  }

  private long revisionCount(@NonNull MeasurementSetId measurementSetId) {
    return requireNonNull(jdbcTemplate.queryForObject(
        """
            SELECT COUNT(*)
              FROM measurement_set_revisions
             WHERE measurement_set_id = :measurementSetId
            """,
        new MapSqlParameterSource("measurementSetId", measurementSetId.value()),
        Long.class
    ));
  }

  private boolean recordExists(@NonNull MeasurementRecordId recordId) {
    return requireNonNull(jdbcTemplate.queryForObject(
        "SELECT EXISTS (SELECT 1 FROM measurement_records WHERE record_id = :recordId)",
        new MapSqlParameterSource("recordId", recordId.value()),
        Boolean.class
    ));
  }

  private boolean measurementSetExists(@NonNull MeasurementSetId measurementSetId) {
    return requireNonNull(jdbcTemplate.queryForObject(
        "SELECT EXISTS (SELECT 1 FROM measurement_sets WHERE measurement_set_id = :measurementSetId)",
        new MapSqlParameterSource("measurementSetId", measurementSetId.value()),
        Boolean.class
    ));
  }

  private <T> @NonNull T inTransaction(@NonNull Supplier<T> action) {
    return requireNonNull(new TransactionTemplate(transactionManager).execute(_ -> action.get()));
  }

  private static void await(@NonNull CyclicBarrier barrier) {
    try {
      barrier.await(10, TimeUnit.SECONDS);
    } catch (Exception exception) {
      throw new AssertionError("concurrent measurement writers did not reach the barrier", exception);
    }
  }

  private static void await(@NonNull CountDownLatch latch) {
    try {
      if (!latch.await(10, TimeUnit.SECONDS)) {
        throw new AssertionError("concurrent measurement operation timed out");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("concurrent measurement operation was interrupted", exception);
    }
  }

  private enum WriteOutcome {
    SUCCESS,
    CONFLICT
  }
}
