package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.batch;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.productionCandidatePrediction;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase.ConsumeFeedstockBatchMaterialService;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase.InspectFeedstockBatchMaterialService;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementFreshnessThresholdProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectCurrentStateProvider;
import org.craft.mcs.cap.core.measurement.application.usecase.CurrentMeasurementEvidenceService;
import org.craft.mcs.cap.core.measurement.application.usecase.CurrentMeasurementResultResolver;
import org.craft.mcs.cap.core.measurement.application.usecase.QueryMeasurementSetMetadataService;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.batch.application.creation.usecase.CreateProductionBatchFromPlanService;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionSourceInventory;
import org.craft.mcs.cap.core.production.batch.application.vessel.port.out.ProductionVesselAssignmentValidator;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.planning.application.authorization.port.out.ProductionPlanningAccessControl;
import org.craft.mcs.cap.core.production.planning.application.ProductionBatchCandidateStaleException;
import org.craft.mcs.cap.core.production.planning.application.confirmation.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.planning.application.confirmation.usecase.CreateProductionBatchService;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionSourceInspector;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidatePotentialAbvView;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateLineageStore;
import org.craft.mcs.cap.core.traceability.application.usecase.AppendBatchTraceStepService;
import org.craft.mcs.cap.core.traceability.application.usecase.GetMaterialStateService;
import org.craft.mcs.cap.core.traceability.application.usecase.QueryMaterialStateLineageService;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.out.production.planning.batch.ProductionBatchConfirmationAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source.FeedstockProductionSourceSnapshotValidator;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.platform.persistence.SpringTransactionHelper;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import(
    {
        PostgresDataJdbcTestConfiguration.class,
        ProductionCreationMaterialFlowDataJdbcTest.FlowTestConfiguration.class,
        SpringTransactionHelper.class,
        CurrentMeasurementResultResolver.class,
        CurrentMeasurementEvidenceService.class,
        QueryMeasurementSetMetadataService.class,
        FeedstockCurrentProfileQueryService.class,
        InspectFeedstockBatchMaterialService.class,
        ConsumeFeedstockBatchMaterialService.class,
        AppendBatchTraceStepService.class,
        GetMaterialStateService.class,
        JdbcProductionBatchRepository.class
    }
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProductionCreationMaterialFlowDataJdbcTest {

  private static final Instant RECORDED_AT = Instant.parse("2026-07-25T10:00:00Z");

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private ProductionSourceInspector sourceInspector;

  @Autowired
  private ProductionSourceInventory sourceInventory;

  @Autowired
  private ProductionBatchCandidateStore candidateStore;

  @Autowired
  private ProductionBatchRepository productionBatchRepository;

  @Autowired
  private ProductionBatchTraceRecorder traceRecorder;

  @Autowired
  private ProductionAccessControl productionAccessControl;

  @Autowired
  private ProductionVesselAssignmentValidator productionVesselAssignmentValidator;

  @Autowired
  private TransactionHelper transactionHelper;

  @Autowired
  private Clock clock;

  private WorkspaceId workspaceId;
  private FeedstockBatchId feedstockBatchId;
  private MaterialStateId initialFeedstockStateId;
  private VesselId feedstockVesselId;
  private VesselId productionVesselId;

  @BeforeEach
  void seedSourceMaterial() {
    workspaceId = WorkspaceId.newId();
    feedstockBatchId = FeedstockBatchId.newId();
    initialFeedstockStateId = MaterialStateId.newId();
    feedstockVesselId = VesselId.newId();
    productionVesselId = VesselId.newId();
    insertWorkspace();
    insertFeedstockBatch();
    insertInitialFeedstockTraceState();
    insertMeasurementSet();
  }

  private void insertInitialFeedstockTraceState() {
    final var stepId = newUuid();
    final var occurredAt = OffsetDateTime.parse(RECORDED_AT.toString());
    jdbcTemplate.update(
        """
            INSERT INTO batch_trace_steps (
                step_id,
                workspace_id,
                step_type,
                occurred_at,
                performed_by_type,
                performed_by_id,
                created_at
            ) VALUES (?, ?, 'FEEDSTOCK_BATCH_CREATED', ?, 'ACCOUNT_USER', ?, ?)
            """,
        stepId,
        workspaceId.value(),
        occurredAt,
        newUuid(),
        occurredAt
    );
    jdbcTemplate.update(
        """
            INSERT INTO batch_trace_step_subjects (
                step_id,
                subject_role,
                subject_order,
                material_state_id,
                subject_type,
                subject_id,
                invalidated_characteristics
            ) VALUES (?, 'OUTPUT', 0, ?, 'FEEDSTOCK_BATCH', ?, ARRAY[]::VARCHAR[])
            """,
        stepId,
        initialFeedstockStateId.value(),
        feedstockBatchId.value()
    );
  }

  @Test
  void commitsFeedstockConsumptionProductionTraceAndCandidateConfirmationTogether() {
    final var source = sourceComponent();
    final var candidate = candidate(source);
    candidateStore.create(candidate);

    final var created = service(candidateStore).create(command(candidate));

    assertThat(created.candidate().status())
        .isEqualTo(ProductionBatchCandidateStatus.CONFIRMED);
    assertThat(number(
        "SELECT current_volume_liters FROM production_batches WHERE batch_id = ?",
        created.productionBatch().batchId().value()
    ))
        .isEqualByComparingTo("100");
    assertThat(number(
        "SELECT available_volume_liters FROM feedstock_batches WHERE batch_id = ?",
        feedstockBatchId.value()
    ))
        .isEqualByComparingTo("150");
    assertThat(count(
        "SELECT COUNT(*) FROM production_batches WHERE workspace_id = ?",
        workspaceId.value()
    ))
        .isOne();
    assertThat(count(
        """
            SELECT COUNT(*)
              FROM batch_trace_steps
             WHERE workspace_id = ?
               AND step_type = 'PRODUCTION_BATCH_CREATED'
            """,
        workspaceId.value()
    ))
        .isOne();
    assertThat(number(
        """
            SELECT snapshot_volume_liters
              FROM batch_trace_step_subjects
              JOIN batch_trace_steps USING (step_id)
             WHERE subject_type = 'FEEDSTOCK_BATCH'
               AND subject_id = ?
               AND subject_role = 'OUTPUT'
               AND step_type = 'PRODUCTION_BATCH_CREATED'
            """,
        feedstockBatchId.value()
    ))
        .isEqualByComparingTo("150");
    assertThat(feedstockCurrentState())
        .isNotEqualTo(initialFeedstockStateId.value());
    assertThat(productionCurrentState(created.productionBatch().batchId().value()))
        .isNotNull();
  }

  @Test
  void rollsBackEveryMaterialChangeWhenCandidateConfirmationPersistenceFails() {
    final var source = sourceComponent();
    final var saved = candidateStore.create(candidate(source));
    final ProductionBatchCandidateStore failingStore = new DelegatingCandidateStore(candidateStore) {
      @Override
      public @NonNull ProductionBatchCandidateSnapshot update(
          @NonNull ProductionBatchCandidateSnapshot snapshot
      ) {
        throw new IllegalStateException("candidate persistence failed");
      }
    };

    assertThatThrownBy(() -> service(failingStore).create(command(saved)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("candidate persistence failed");
    assertThat(number(
        "SELECT available_volume_liters FROM feedstock_batches WHERE batch_id = ?",
        feedstockBatchId.value()
    ))
        .isEqualByComparingTo("250");
    assertThat(count(
        "SELECT COUNT(*) FROM production_batches WHERE workspace_id = ?",
        workspaceId.value()
    ))
        .isZero();
    assertThat(count(
        """
            SELECT COUNT(*)
              FROM batch_trace_steps
             WHERE workspace_id = ?
               AND step_type = 'PRODUCTION_BATCH_CREATED'
            """,
        workspaceId.value()
    ))
        .isZero();
    assertThat(feedstockCurrentState())
        .isEqualTo(initialFeedstockStateId.value());
    assertThat(candidateStore.findById(saved.candidate().candidateId()))
        .get()
        .extracting(ProductionBatchCandidateSnapshot::candidate)
        .returns(ProductionBatchCandidateStatus.AVAILABLE, ProductionBatchCandidate::status);
  }

  @Test
  void reportsStaleCandidateBeforeInsufficientVolumeAndDoesNotConsumeSource() {
    final var candidate = candidate(sourceComponent());
    final var saved = candidateStore.create(candidate);
    final var changedStateId = MaterialStateId.newId();
    jdbcTemplate.update(
        """
            UPDATE feedstock_batches
               SET available_volume_liters = 50,
                   current_material_state_id = ?,
                   version = version + 1
             WHERE batch_id = ?
            """,
        changedStateId.value(),
        feedstockBatchId.value()
    );

    assertThatThrownBy(() -> service(candidateStore).create(command(saved)))
        .isInstanceOf(ProductionBatchCandidateStaleException.class);
    assertThat(number(
        "SELECT available_volume_liters FROM feedstock_batches WHERE batch_id = ?",
        feedstockBatchId.value()
    ))
        .isEqualByComparingTo("50");
    assertThat(count(
        "SELECT COUNT(*) FROM production_batches WHERE workspace_id = ?",
        workspaceId.value()
    ))
        .isZero();
    assertThat(candidateStore.findById(saved.candidate().candidateId()))
        .get()
        .extracting(ProductionBatchCandidateSnapshot::candidate)
        .returns(ProductionBatchCandidateStatus.AVAILABLE, ProductionBatchCandidate::status);
  }

  @Test
  void concurrentConfirmationConsumesSourceAndConfirmsCandidateExactlyOnce()
      throws Exception {
    final var source = sourceComponent("200");
    final var saved = candidateStore.create(candidate(source));
    final var secondVesselId = VesselId.newId();
    VesselTestFixtures.persist(jdbcTemplate, workspaceId, secondVesselId, "Tank P-02");
    final var start = new CountDownLatch(1);
    final var creationService = service(candidateStore);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var first = executor.submit(() -> createAfter(
          start,
          creationService,
          command(saved, productionVesselId)
      ));
      final var second = executor.submit(() -> createAfter(
          start,
          creationService,
          command(saved, secondVesselId)
      ));
      start.countDown();
      final var attempts = List.of(
          first.get(10, TimeUnit.SECONDS),
          second.get(10, TimeUnit.SECONDS)
      );

      assertThat(attempts)
          .filteredOn(CreationAttempt::succeeded)
          .hasSize(1);
      assertThat(attempts)
          .filteredOn(attempt -> attempt.failure() != null)
          .extracting(CreationAttempt::failure)
          .allSatisfy(failure -> assertThat(failure)
              .isInstanceOf(DomainConflictException.class)
              .hasMessage("production batch candidate has already been confirmed"))
          .hasSize(1);
      assertThat(number(
          "SELECT available_volume_liters FROM feedstock_batches WHERE batch_id = ?",
          feedstockBatchId.value()
      ))
          .isEqualByComparingTo("50");
      assertThat(count(
          "SELECT COUNT(*) FROM production_batches WHERE workspace_id = ?",
          workspaceId.value()
      )).isOne();
      assertThat(count(
          """
              SELECT COUNT(*)
                FROM batch_trace_steps
               WHERE workspace_id = ?
                 AND step_type = 'PRODUCTION_BATCH_CREATED'
              """,
          workspaceId.value()
      )).isOne();
      assertThat(candidateStore.findById(saved.candidate().candidateId()))
          .get()
          .extracting(ProductionBatchCandidateSnapshot::candidate)
          .returns(ProductionBatchCandidateStatus.CONFIRMED, ProductionBatchCandidate::status);
    }
  }

  private @NonNull CreateProductionBatchService service(
      @NonNull ProductionBatchCandidateStore store
  ) {
    final var batchCreationService = new CreateProductionBatchFromPlanService(
        sourceInventory,
        productionBatchRepository,
        traceRecorder,
        productionAccessControl,
        productionVesselAssignmentValidator,
        transactionHelper,
        clock
    );
    return new CreateProductionBatchService(
        store,
        new ProductionBatchConfirmationAdapter(batchCreationService),
        new PermitAllProductionPlanningAccessControl(),
        transactionHelper
    );
  }

  private static final class PermitAllProductionPlanningAccessControl
      implements ProductionPlanningAccessControl {

    @Override
    public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
    }

    @Override
    public void requireWriteAccess(@NonNull WorkspaceId workspaceId) {
    }
  }

  private @NonNull ProductionBatchCandidateSnapshot candidate(
      @NonNull ProductionSourceComponent source
  ) {
    final var inspected = sourceInspector.inspect(workspaceId, source);
    final var composition = ProductionSourceComposition.of(List.of(source));
    final var profile = productionProfile();
    return new ProductionBatchCandidateSnapshot(
        ProductionBatchCandidate.create(
            ProductionBatchCandidateId.newId(),
            workspaceId,
            composition,
            Map.of(
                source.reference(),
                new ProductionCandidateSourceEvidence(
                    inspected.evidenceReference(),
                    inspected.materialStateId()
                )
            ),
            profile
        ),
        potentialAbv(profile),
        null,
        null,
        List.of()
    );
  }

  private static @NonNull ProductionCandidatePotentialAbvView potentialAbv(
      @NonNull ProductionProfileSnapshot profile
  ) {
    return productionCandidatePrediction(profile.specificGravity());
  }

  private @NonNull ProductionSourceComponent sourceComponent() {
    return sourceComponent("100");
  }

  private @NonNull ProductionSourceComponent sourceComponent(@NonNull String volume) {
    return new ProductionSourceComponent(
        ProductionSourceReference.feedstockBatch(feedstockBatchId.value()),
        VolumeValue.ofLiters(volume)
    );
  }

  private @NonNull CreateProductionBatchCommand command(
      @NonNull ProductionBatchCandidateSnapshot candidate
  ) {
    return command(candidate, productionVesselId);
  }

  private @NonNull CreateProductionBatchCommand command(
      @NonNull ProductionBatchCandidateSnapshot candidate,
      @NonNull VesselId vesselId
  ) {
    return new CreateProductionBatchCommand(
        workspaceId,
        BatchName.of("Integrated production"),
        vesselId,
        candidate.candidate().candidateId(),
        null
    );
  }

  private CreationAttempt createAfter(
      CountDownLatch start,
      CreateProductionBatchService service,
      CreateProductionBatchCommand command
  ) {
    await(start);
    try {
      service.create(command);
      return CreationAttempt.success();
    } catch (RuntimeException exception) {
      return CreationAttempt.failure(exception);
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      if (!latch.await(10, TimeUnit.SECONDS)) {
        throw new AssertionError("timed out while waiting for production concurrency test coordination");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("production concurrency test was interrupted", exception);
    }
  }

  private static @NonNull ProductionProfileSnapshot productionProfile() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        TanninValue.of("1200")
    );
  }

  private void insertWorkspace() {
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, 'Integrated flow', 'CIDERY', 'ACTIVE', 0, ?, ?)
            """,
        workspaceId.value(),
        OffsetDateTime.parse("2026-07-25T10:00:00Z"),
        OffsetDateTime.parse("2026-07-25T10:00:00Z")
    );
    VesselTestFixtures.persist(jdbcTemplate, workspaceId, feedstockVesselId, "Tank A-01");
    VesselTestFixtures.persist(jdbcTemplate, workspaceId, productionVesselId, "Tank P-01");
  }

  private void insertFeedstockBatch() {
    jdbcTemplate.update(
        """
            INSERT INTO feedstock_batches (
                batch_id,
                workspace_id,
                name,
                vessel_id,
                type,
                season_starts_on,
                season_ends_on,
                lifecycle,
                available_volume_liters,
                current_material_state_id,
                version,
                created_at,
                updated_at
            ) VALUES (
                ?,
                ?,
                'Integrated juice',
                ?,
                'JUICE',
                DATE '2026-01-01',
                DATE '2026-12-31',
                'ACTIVE',
                250,
                ?,
                1,
                ?,
                ?
            )
            """,
        feedstockBatchId.value(),
        workspaceId.value(),
        feedstockVesselId.value(),
        initialFeedstockStateId.value(),
        OffsetDateTime.parse("2026-07-25T10:00:00Z"),
        OffsetDateTime.parse("2026-07-25T10:00:00Z")
    );
  }

  private void insertMeasurementSet() {
    final var measurementSetId = newUuid();
    final var revisionId = newUuid();
    jdbcTemplate.update(
        """
            INSERT INTO measurement_sets (
                measurement_set_id,
                workspace_id,
                subject_type,
                subject_id,
                version,
                created_at
            ) VALUES (?, ?, 'FEEDSTOCK_BATCH', ?, 0, ?)
            """,
        measurementSetId,
        workspaceId.value(),
        feedstockBatchId.value(),
        OffsetDateTime.parse("2026-07-25T10:00:00Z")
    );
    jdbcTemplate.update(
        """
            INSERT INTO measurement_set_revisions (
                revision_id,
                measurement_set_id,
                revision_number,
                material_state_id,
                lifecycle,
                change_kind,
                name,
                created_at,
                performed_by_type,
                performed_by_id
            ) VALUES (?, ?, 1, ?, 'ACTIVE', 'INITIAL', 'Integrated chemistry', ?, 'ACCOUNT_USER', ?)
            """,
        revisionId,
        measurementSetId,
        initialFeedstockStateId.value(),
        OffsetDateTime.parse("2026-07-25T10:00:00Z"),
        AccountUserId.newId().value()
    );
    jdbcTemplate.update(
        """
            INSERT INTO measurement_set_heads (
                measurement_set_id,
                revision_id
            ) VALUES (?, ?)
            """,
        measurementSetId,
        revisionId
    );
    insertMeasurementRecord(measurementSetId, revisionId, "SPECIFIC_GRAVITY", "1.052");
    insertMeasurementRecord(measurementSetId, revisionId, "TITRATABLE_ACIDITY", "6.2");
    insertMeasurementRecord(measurementSetId, revisionId, "TANNIN", "1200");
  }

  private void insertMeasurementRecord(
      @NonNull UUID measurementSetId,
      @NonNull UUID revisionId,
      @NonNull String characteristic,
      @NonNull String value
  ) {
    final var recordId = newUuid();
    jdbcTemplate.update(
        """
            INSERT INTO measurement_records (
                measurement_set_id,
                record_id,
                characteristic,
                role,
                value_kind,
                measured_at,
                source_type,
                confidence,
                point_value
            ) VALUES (?, ?, ?, 'RESULT', 'POINT', ?, 'LAB', 'LAB_VERIFIED', ?)
            """,
        measurementSetId,
        recordId,
        characteristic,
        OffsetDateTime.parse("2026-07-25T10:00:00Z"),
        new BigDecimal(value)
    );
    jdbcTemplate.update(
        """
            INSERT INTO measurement_revision_records (
                measurement_set_id,
                revision_id,
                record_id,
                characteristic,
                role,
                record_status
            ) VALUES (?, ?, ?, ?, 'RESULT', 'ACTIVE')
            """,
        measurementSetId,
        revisionId,
        recordId,
        characteristic
    );
  }

  private long count(
      @NonNull String sql,
      Object... arguments
  ) {
    return requireNonNull(jdbcTemplate.queryForObject(
        sql,
        Long.class,
        arguments
    ), "count query result cannot be null");
  }

  private @NonNull BigDecimal number(
      @NonNull String sql,
      Object... arguments
  ) {
    return requireNonNull(jdbcTemplate.queryForObject(
        sql,
        BigDecimal.class,
        arguments
    ), "expected numeric query result cannot be null");
  }

  private @NonNull UUID feedstockCurrentState() {
    return requireNonNull(jdbcTemplate.queryForObject(
        """
            SELECT current_material_state_id
              FROM feedstock_batches
             WHERE batch_id = ?
            """,
        UUID.class,
        feedstockBatchId.value()
    ), "expected feedstock current material state cannot be null");
  }

  private @NonNull UUID productionCurrentState(@NonNull UUID productionBatchId) {
    return requireNonNull(jdbcTemplate.queryForObject(
        """
            SELECT current_material_state_id
              FROM production_batches
             WHERE batch_id = ?
            """,
        UUID.class,
        productionBatchId
    ), "expected production current material state cannot be null");
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(
      basePackages = {
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch",
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.query",
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement",
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability",
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.planning",
          "org.craft.mcs.cap.infrastructure.adapter.out.feedstock.batch.measurement",
          "org.craft.mcs.cap.infrastructure.adapter.out.traceability.feedstock",
          "org.craft.mcs.cap.infrastructure.adapter.out.traceability.measurement",
          "org.craft.mcs.cap.infrastructure.adapter.out.traceability.production",
          "org.craft.mcs.cap.infrastructure.adapter.out.production.batch.source",
          "org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source"
      }, excludeFilters = {
          @ComponentScan.Filter(
              type = FilterType.ANNOTATION,
              classes = TestConfiguration.class
          ),
          @ComponentScan.Filter(
              type = FilterType.ASSIGNABLE_TYPE,
              classes = FeedstockProductionSourceSnapshotValidator.class
          )
      }
  )
  static class FlowTestConfiguration {

    @Bean
    FeedstockAccessControl feedstockAccessControl() {
      return new FeedstockAccessControl() {
        @Override
        public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
        }

        @Override
        public void requireWriteAccess(@NonNull WorkspaceId workspaceId) {
        }
      };
    }

    @Bean
    ProductionAccessControl productionAccessControl() {
      return new ProductionAccessControl() {
        @Override
        public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
        }

        @Override
        public void requireWriteAccess(@NonNull WorkspaceId workspaceId) {
        }
      };
    }

    @Bean
    ProductionVesselAssignmentValidator productionVesselAssignmentValidator() {
      return (workspaceId, vesselId, materialVolume) -> VesselTestFixtures.reference(vesselId, "Tank P-01");
    }

    @Bean
    MeasurementSubjectAccessControl measurementSubjectAccessControl() {
      return new MeasurementSubjectAccessControl() {
        @Override
        public @NonNull WorkspaceId requireReadAccess(@NonNull MeasurementSubjectReference subject) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void requireReadAccess(
            @NonNull MeasurementSubjectType subjectType,
            @NonNull WorkspaceId workspaceId
        ) {
        }

        @Override
        public @NonNull WorkspaceId requireWriteAccess(@NonNull MeasurementSubjectReference subject) {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Bean
    MeasurementFreshnessThresholdProvider measurementFreshnessThresholdProvider() {
      return workspaceId -> Duration.ofDays(14);
    }

    @Bean
    MeasurementSubjectCurrentStateProvider currentMaterialStateProvider(JdbcTemplate jdbcTemplate) {
      return (workspaceId, subjects) -> {
        final Map<MeasurementSubjectReference, MaterialStateId> states = new LinkedHashMap<>();
        subjects.forEach(subject -> states.put(
            subject,
            MaterialStateId.of(requireMaterialStateId(jdbcTemplate, workspaceId, subject))
        ));
        return Map.copyOf(states);
      };
    }

    private static @NonNull UUID requireMaterialStateId(
        @NonNull JdbcTemplate jdbcTemplate,
        @NonNull WorkspaceId workspaceId,
        @NonNull MeasurementSubjectReference subject
    ) {
      return switch (subject.type()) {
        case FEEDSTOCK_BATCH -> requireNonNull(jdbcTemplate.queryForObject(
            """
                SELECT current_material_state_id
                  FROM feedstock_batches
                 WHERE workspace_id = ?
                   AND batch_id = ?
                """,
            UUID.class,
            workspaceId.value(),
            subject.id()
        ));
        case PRODUCTION_BATCH -> requireNonNull(jdbcTemplate.queryForObject(
            """
                SELECT current_material_state_id
                  FROM production_batches
                 WHERE workspace_id = ?
                   AND batch_id = ?
                """,
            UUID.class,
            workspaceId.value(),
            subject.id()
        ));
      };
    }

    @Bean
    QueryMaterialStateLineageService queryMaterialStateLineageService(
        MaterialStateLineageStore lineageStore,
        BatchTraceabilityAccessControl accessControl
    ) {
      return new QueryMaterialStateLineageService(lineageStore, accessControl);
    }

    @Bean
    BatchTraceabilityAccessControl batchTraceabilityAccessControl() {
      return new BatchTraceabilityAccessControl() {
        @Override
        public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
        }

        @Override
        public void requireWriteAccess(
            @NonNull WorkspaceId workspaceId,
            @NonNull Set<@NonNull BatchTraceSubjectType> subjectTypes
        ) {
        }
      };
    }

    @Bean
    GetCurrentAccountProfileUseCase getCurrentAccountProfileUseCase() {
      return () -> new AccountUserView(
          AccountUserId.newId(),
          EmailAddress.of("technologist@example.com"),
          DisplayName.of("Technologist"),
          null,
          AccountUserStatus.ACTIVE
      );
    }
  }

  private static class DelegatingCandidateStore
      implements ProductionBatchCandidateStore {

    private final @NonNull ProductionBatchCandidateStore delegate;

    private DelegatingCandidateStore(@NonNull ProductionBatchCandidateStore delegate) {
      this.delegate = delegate;
    }

    @Override
    public @NonNull ProductionBatchCandidateSnapshot create(
        @NonNull ProductionBatchCandidateSnapshot snapshot
    ) {
      return delegate.create(snapshot);
    }

    @Override
    public @NonNull ProductionBatchCandidateSnapshot update(
        @NonNull ProductionBatchCandidateSnapshot snapshot
    ) {
      return delegate.update(snapshot);
    }

    @Override
    public @NonNull Optional<@NonNull ProductionBatchCandidateSnapshot> findById(
        @NonNull ProductionBatchCandidateId candidateId
    ) {
      return delegate.findById(candidateId);
    }

    @Override
    public @NonNull Optional<@NonNull ProductionBatchCandidateSnapshot> lockById(
        @NonNull ProductionBatchCandidateId candidateId
    ) {
      return delegate.lockById(candidateId);
    }
  }

  private record CreationAttempt(
      boolean succeeded,
      @Nullable RuntimeException failure
  ) {

    private static CreationAttempt success() {
      return new CreationAttempt(true, null);
    }

    private static CreationAttempt failure(RuntimeException exception) {
      return new CreationAttempt(false, requireNonNull(exception));
    }
  }
}
