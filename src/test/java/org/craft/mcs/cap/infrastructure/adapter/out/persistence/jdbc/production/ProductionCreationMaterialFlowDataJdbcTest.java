package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

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
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateLineageStore;
import org.craft.mcs.cap.core.traceability.application.usecase.AppendBatchTraceStepService;
import org.craft.mcs.cap.core.traceability.application.usecase.GetMaterialStateService;
import org.craft.mcs.cap.core.traceability.application.usecase.QueryMaterialStateLineageService;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.inventory.usecase.ConsumeFeedstockBatchMaterialService;
import org.craft.mcs.cap.core.feedstock.application.inventory.usecase.InspectFeedstockBatchMaterialService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMaterialStateProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementFreshnessThresholdProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.application.usecase.CurrentMaterialStateProjector;
import org.craft.mcs.cap.core.measurement.application.usecase.CurrentMeasurementResultResolver;
import org.craft.mcs.cap.core.measurement.application.usecase.QueryMeasurementSetMetadataService;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.candidate.application.confirmation.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceInventory;
import org.craft.mcs.cap.core.production.application.vessel.port.out.ProductionVesselAssignmentValidator;
import org.craft.mcs.cap.core.production.candidate.application.confirmation.usecase.CreateProductionBatchService;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionSourceInspector;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.platform.persistence.SpringTransactionHelper;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.prediction;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import(
    {
        PostgresDataJdbcTestConfiguration.class,
        ProductionCreationMaterialFlowDataJdbcTest.FlowTestConfiguration.class,
        SpringTransactionHelper.class,
        CurrentMeasurementResultResolver.class,
        CurrentMaterialStateProjector.class,
        QueryMeasurementSetMetadataService.class,
        FeedstockCurrentProfileQueryService.class,
        InspectFeedstockBatchMaterialService.class,
        ConsumeFeedstockBatchMaterialService.class,
        AppendBatchTraceStepService.class,
        GetMaterialStateService.class,
        CreateProductionBatchService.class,
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
    assertThat(created.productionBatch())
        .returns(VolumeValue.ofLiters("100"), ProductionBatchView::currentVolume);
    assertThat(number("SELECT available_volume_liters FROM feedstock_batches WHERE batch_id = ?"))
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
    assertThat(number("""
        SELECT snapshot_volume_liters
          FROM batch_trace_step_subjects
          JOIN batch_trace_steps USING (step_id)
         WHERE subject_type = 'FEEDSTOCK_BATCH'
           AND subject_id = ?
           AND subject_role = 'OUTPUT'
           AND step_type = 'PRODUCTION_BATCH_CREATED'
        """))
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
    assertThat(number("SELECT available_volume_liters FROM feedstock_batches WHERE batch_id = ?"))
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

  private @NonNull CreateProductionBatchService service(
      @NonNull ProductionBatchCandidateStore store
  ) {
    return new CreateProductionBatchService(
        sourceInventory,
        store,
        productionBatchRepository,
        traceRecorder,
        productionAccessControl,
        productionVesselAssignmentValidator,
        transactionHelper,
        clock
    );
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

  private static @NonNull PotentialAbvPrediction potentialAbv(
      @NonNull ProductionProfileSnapshot profile
  ) {
    return prediction(profile.specificGravity());
  }

  private @NonNull ProductionSourceComponent sourceComponent() {
    return new ProductionSourceComponent(
        ProductionSourceReference.feedstockBatch(feedstockBatchId.value()),
        VolumeValue.ofLiters("100")
    );
  }

  private @NonNull CreateProductionBatchCommand command(
      @NonNull ProductionBatchCandidateSnapshot candidate
  ) {
    return new CreateProductionBatchCommand(
        workspaceId,
        BatchName.of("Integrated production"),
        productionVesselId,
        candidate.candidate().candidateId(),
        null
    );
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
                lifecycle,
                version,
                created_at,
                updated_at
            ) VALUES (?, ?, 'FEEDSTOCK_BATCH', ?, 'ACTIVE', 0, ?, ?)
            """,
        measurementSetId,
        workspaceId.value(),
        feedstockBatchId.value(),
        OffsetDateTime.parse("2026-07-25T10:00:00Z"),
        OffsetDateTime.parse("2026-07-25T10:00:00Z")
    );
    jdbcTemplate.update(
        """
            INSERT INTO measurement_set_revisions (
                revision_id,
                measurement_set_id,
                revision_number,
                material_state_id,
                name,
                created_at,
                actor_reference
            ) VALUES (?, ?, 1, ?, 'Integrated chemistry', ?, ?)
            """,
        revisionId,
        measurementSetId,
        initialFeedstockStateId.value(),
        OffsetDateTime.parse("2026-07-25T10:00:00Z"),
        "ACCOUNT_USER:" + AccountUserId.newId().value()
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
    insertMeasurementRecord(revisionId, "SPECIFIC_GRAVITY", "1.052");
    insertMeasurementRecord(revisionId, "TITRATABLE_ACIDITY", "6.2");
    insertMeasurementRecord(revisionId, "TANNIN", "1200");
  }

  private void insertMeasurementRecord(
      @NonNull UUID revisionId,
      @NonNull String characteristic,
      @NonNull String value
  ) {
    final var recordId = newUuid();
    jdbcTemplate.update(
        """
            INSERT INTO measurement_records (
                record_id,
                characteristic,
                role,
                value_kind,
                measured_at,
                source_type,
                confidence,
                point_value
            ) VALUES (?, ?, 'RESULT', 'POINT', ?, 'LAB', 'LAB_VERIFIED', ?)
            """,
        recordId,
        characteristic,
        OffsetDateTime.parse("2026-07-25T10:00:00Z"),
        new BigDecimal(value)
    );
    jdbcTemplate.update(
        """
            INSERT INTO measurement_revision_records (
                revision_id,
                record_id,
                characteristic,
                role,
                record_status
            ) VALUES (?, ?, ?, 'RESULT', 'ACTIVE')
            """,
        revisionId,
        recordId,
        characteristic
    );
  }

  private long count(
      @NonNull String sql,
      Object... arguments
  ) {
    final var result = jdbcTemplate.queryForObject(sql, Long.class, arguments);
    return result == null ? 0L : result; // NOSONAR
  }

  private @NonNull BigDecimal number(@NonNull String sql) {
    return jdbcTemplate.queryForObject(
        sql,
        BigDecimal.class,
        feedstockBatchId.value()
    );
  }

  private @NonNull UUID feedstockCurrentState() {
    return jdbcTemplate.queryForObject(
        """
            SELECT current_material_state_id
              FROM feedstock_batches
             WHERE batch_id = ?
            """,
        UUID.class,
        feedstockBatchId.value()
    );
  }

  private @NonNull UUID productionCurrentState(@NonNull UUID productionBatchId) {
    return jdbcTemplate.queryForObject(
        """
            SELECT current_material_state_id
              FROM production_batches
             WHERE batch_id = ?
            """,
        UUID.class,
        productionBatchId
    );
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(
      basePackages = {
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch",
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query",
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement",
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability",
          "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate",
          "org.craft.mcs.cap.infrastructure.adapter.out.feedstock.measurement",
          "org.craft.mcs.cap.infrastructure.adapter.out.traceability.feedstock",
          "org.craft.mcs.cap.infrastructure.adapter.out.traceability.measurement",
          "org.craft.mcs.cap.infrastructure.adapter.out.traceability.production",
          "org.craft.mcs.cap.infrastructure.adapter.out.production.source"
      }, excludeFilters = @ComponentScan.Filter(
      type = FilterType.ANNOTATION,
      classes = TestConfiguration.class
  )
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
    CurrentMaterialStateProvider currentMaterialStateProvider(JdbcTemplate jdbcTemplate) {
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
  }
}
