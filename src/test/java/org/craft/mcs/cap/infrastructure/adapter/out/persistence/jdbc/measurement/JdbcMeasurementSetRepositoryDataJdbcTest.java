package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.application.MeasurementEvidenceIdentityConflictException;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetAlreadyExistsException;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementResultRetractionView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetMetadataView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetRetractionView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementMaterialStateLineageProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementResultEvidenceQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetMetadataQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.core.measurement.application.usecase.CurrentMeasurementResultResolver;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementResultRetraction;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionChangeKind;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceReference;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.traceability.application.port.in.QueryMaterialStateLineageUseCase;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateLineageStore;
import org.craft.mcs.cap.core.traceability.application.usecase.QueryMaterialStateLineageService;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.pointValue;
import static org.mockito.Mockito.mock;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
    {
        PostgresDataJdbcTestConfiguration.class,
        JdbcMeasurementSetRepositoryDataJdbcTest.MeasurementJdbcAdaptersTestConfiguration.class
    }
)
class JdbcMeasurementSetRepositoryDataJdbcTest {

  private static final Instant RECORDED_AT = Instant.parse("2026-07-20T10:00:00Z");

  @Autowired
  private MeasurementSetRepository repository;

  @Autowired
  private CurrentMeasurementResultResolver currentResultResolver;

  @Autowired
  private MeasurementSetViewQuery viewQuery;

  @Autowired
  private MeasurementSetMetadataQuery metadataQuery;

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
                'Measurement test workspace',
                'CIDERY',
                'ACTIVE',
                0,
                :createdAt,
                :updatedAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("workspaceId", workspaceId.value())
            .addValue("createdAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
            .addValue("updatedAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
    );
    VesselTestFixtures.persistAllVessels(jdbcTemplate.getJdbcOperations(), workspaceId);
  }

  @Test
  void preservesDerivedObservationsProvenanceAndCanonicalUnits() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        newUuid()
    );
    final var firstObservation = observation("1.050", "2026-07-20T09:58:00Z");
    final var secondObservation = observation("1.052", "2026-07-20T10:00:00Z");
    final var result = result(firstObservation, secondObservation);
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, List.of(firstObservation, secondObservation, result))
    );

    final var created = repository.create(set);
    initializeCurrentState(subject, set.currentRevision().materialStateId());

    final var restored = repository.findVersionedById(set.measurementSetId()).orElseThrow();
    assertThat(restored.aggregate().currentRevision().records())
        .hasSize(3)
        .filteredOn(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
        .singleElement()
        .returns(MeasurementDerivationMethod.ARITHMETIC_MEAN, MeasurementRecord::derivationMethod)
        .returns(
            List.of(firstObservation.recordId(), secondObservation.recordId()),
            MeasurementRecord::derivationInputs
        )
        .satisfies(measurementRecord -> assertThat(measurementRecord.provenance())
            .returns(ConfidenceLevel.HIGH, MeasurementProvenance::confidence))
        .satisfies(measurementRecord -> assertThat(pointValue(measurementRecord.value()))
            .isInstanceOfSatisfying(
                SpecificGravityValue.class, value -> assertThat(value)
                    .returns(BeverageUnits.SG, SpecificGravityValue::unit)
                    .satisfies(specificGravity -> assertThat(specificGravity.asBigDecimal())
                        .isEqualByComparingTo("1.051"))
            ));

    assertThat(currentResultResolver.resolve(
        workspaceId,
        Map.of(subject, set.currentRevision().materialStateId())
    ))
        .singleElement()
        .returns(result.recordId(), CurrentMeasurementResultView::recordId)
        .returns(MeasurementCharacteristic.SPECIFIC_GRAVITY, CurrentMeasurementResultView::characteristic)
        .satisfies(current -> assertThat(current.provenance())
            .returns(MeasurementSourceType.DERIVED, MeasurementProvenance::sourceType)
            .returns(ConfidenceLevel.HIGH, MeasurementProvenance::confidence));
    assertThat(viewQuery.findBySubject(workspaceId, subject))
        .singleElement()
        .satisfies(view -> assertThat(view)
            .returns(created.revision().value(), MeasurementSetView::version)
            .returns(set.measurementSetId(), MeasurementSetView::measurementSetId)
            .satisfies(value -> assertThat(value.records())
                .hasSize(3)
                .filteredOn(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
                .singleElement()
                .returns(
                    List.of(firstObservation.recordId(), secondObservation.recordId()),
                    MeasurementRecordView::derivationInputs
                )));
    assertThat(metadataQuery.findAll(workspaceId, Set.of(set.measurementSetId())))
        .containsKey(set.measurementSetId())
        .extractingByKey(set.measurementSetId())
        .returns(set.measurementSetId(), MeasurementSetMetadataView::measurementSetId)
        .returns(set.currentRevision().name(), MeasurementSetMetadataView::name)
        .returns(created.revision().value(), MeasurementSetMetadataView::version);
  }

  @Test
  void persistsTypedMachineIdentityActorWithoutStringEncoding() {
    final var actor = ActorReference.machineIdentity(newUuid());
    final var revision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.initial(),
        MaterialStateId.newId(),
        MeasurementSetName.of("Imported pH result"),
        null,
        null,
        RECORDED_AT,
        actor,
        List.of(directResult(
            MeasurementCharacteristic.PH,
            point(MeasurementCharacteristic.PH, "3.40")
        )),
        List.of()
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        revision
    );

    repository.create(set);

    assertThat(repository.findVersionedById(set.measurementSetId()))
        .get()
        .extracting(VersionedAggregate::aggregate)
        .extracting(MeasurementSet::currentRevision)
        .returns(actor, MeasurementSetRevision::actor);
    assertThat(viewQuery.findById(set.measurementSetId()))
        .get()
        .returns(actor, MeasurementSetView::actor);
    assertThat(jdbcTemplate.queryForMap(
        """
            SELECT performed_by_type, performed_by_id
              FROM measurement_set_revisions
             WHERE revision_id = :revisionId
            """,
        new MapSqlParameterSource("revisionId", revision.revisionId().value())
    ))
        .containsEntry("performed_by_type", ActorType.MACHINE_IDENTITY.name())
        .containsEntry("performed_by_id", actor.id());
  }

  @Test
  void recordEvidenceCannotBeAssociatedWithAnotherMeasurementSet() {
    final var firstRecord = directResult(
        MeasurementCharacteristic.TANNIN,
        point(MeasurementCharacteristic.TANNIN, "1200")
    );
    final var firstSet = measurementSet(List.of(firstRecord));
    final var secondSet = measurementSet(List.of(directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.40")
    )));
    repository.create(firstSet);
    repository.create(secondSet);

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            INSERT INTO measurement_revision_records (
                measurement_set_id,
                revision_id,
                record_id,
                characteristic,
                role,
                record_status
            ) VALUES (
                :measurementSetId,
                :revisionId,
                :recordId,
                'TANNIN',
                'RESULT',
                'ACTIVE'
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", secondSet.measurementSetId().value())
            .addValue("revisionId", secondSet.currentRevision().revisionId().value())
            .addValue("recordId", firstRecord.recordId().value())
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("fk_measurement_revision_records_record");
  }

  @Test
  void rejectsCurrentMaterialStateThatIsAbsentFromImmutableTrace() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );

    assertThatThrownBy(() -> currentResultResolver.resolve(
        workspaceId,
        Map.of(subject, MaterialStateId.newId())
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("lineage must start with its current material state at depth zero");
  }

  @Test
  void reusesImmutableDerivedEvidenceAcrossCorrections() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var firstObservation = observation("1.050", "2026-07-20T09:58:00Z");
    final var secondObservation = observation("1.052", "2026-07-20T10:00:00Z");
    final var result = result(firstObservation, secondObservation);
    final var initialRevision = revision(
        1,
        List.of(firstObservation, secondObservation, result)
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        initialRevision
    );
    final var created = repository.create(set);
    final var correctedRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        initialRevision.materialStateId(),
        initialRevision.name(),
        "Corrected note",
        MeasurementCorrectionReason.of("Clarified the laboratory note"),
        RECORDED_AT.plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        initialRevision.records(),
        List.of()
    );

    set.correct(correctedRevision);
    repository.appendRevision(created.withAggregate(set));

    assertThat(repository.findVersionedById(set.measurementSetId()).orElseThrow())
        .returns(1L, restored -> restored.revision().value())
        .extracting(restored -> restored.aggregate().currentRevision())
        .returns(correctedRevision.revisionId(), MeasurementSetRevision::revisionId)
        .satisfies(revision -> assertThat(revision.records())
            .extracting(MeasurementRecord::recordId)
            .containsExactlyInAnyOrder(
                firstObservation.recordId(),
                secondObservation.recordId(),
                result.recordId()
            ))
        .satisfies(revision -> assertThat(revision.records())
            .filteredOn(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
            .singleElement()
            .returns(
                List.of(firstObservation.recordId(), secondObservation.recordId()),
                MeasurementRecord::derivationInputs
            ));
  }

  @Test
  void rejectsRebindingAnImmutableRecordIdentityToDifferentEvidence() {
    final var sharedRecordId = MeasurementRecordId.newId();
    final var firstResult = new MeasurementRecord(
        sharedRecordId,
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.PH, "3.40"),
        RECORDED_AT,
        new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null),
        null,
        List.of()
    );
    final var conflictingResult = new MeasurementRecord(
        sharedRecordId,
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.PH, "3.20"),
        RECORDED_AT,
        new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null),
        null,
        List.of()
    );
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        revision(1, List.of(firstResult))
    ));
    final var conflictingSet = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        revision(1, List.of(conflictingResult))
    );

    assertThatThrownBy(() -> repository.create(conflictingSet))
        .isInstanceOf(MeasurementEvidenceIdentityConflictException.class)
        .hasMessageContaining(sharedRecordId.value().toString());
  }

  private void initializeCurrentState(
      MeasurementSubjectReference subject,
      MaterialStateId materialStateId
  ) {
    switch (subject.type()) {
      case FEEDSTOCK_BATCH -> insertFeedstockBatch(subject, materialStateId);
      case PRODUCTION_BATCH -> insertProductionBatch(subject, materialStateId);
    }
    insertInitialTraceState(subject, materialStateId);
  }

  private void insertInitialTraceState(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId materialStateId
  ) {
    final var stepId = newUuid();
    final var occurredAt = OffsetDateTime.parse("2026-07-20T10:00:00Z");
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
            ) VALUES (
                :stepId,
                :workspaceId,
                :stepType,
                :occurredAt,
                :performedByType,
                :performedById,
                :createdAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("stepId", stepId)
            .addValue("workspaceId", workspaceId.value())
            .addValue(
                "stepType",
                subject.type() == MeasurementSubjectType.FEEDSTOCK_BATCH
                    ? "FEEDSTOCK_BATCH_CREATED"
                    : "PRODUCTION_BATCH_CREATED"
            )
            .addValue("performedByType", ActorType.MACHINE_IDENTITY.name())
            .addValue("performedById", newUuid())
            .addValue("occurredAt", occurredAt)
            .addValue("createdAt", occurredAt)
    );
    insertStateTransitionSubject(stepId, subject, materialStateId, "OUTPUT", false);
  }

  private void insertFeedstockBatch(
      MeasurementSubjectReference subject,
      MaterialStateId materialStateId
  ) {
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
                :batchId,
                :workspaceId,
                'Measurement feedstock',
                :vesselId,
                'JUICE',
                DATE '2026-01-01',
                DATE '2026-12-31',
                'ACTIVE',
                100,
                :materialStateId,
                0,
                :createdAt,
                :updatedAt
            )
            """,
        materialSubjectParameters(subject, materialStateId)
    );
  }

  private void insertProductionBatch(
      MeasurementSubjectReference subject,
      MaterialStateId materialStateId
  ) {
    jdbcTemplate.update(
        """
            INSERT INTO production_batches (
                batch_id,
                workspace_id,
                name,
                vessel_id,
                lifecycle,
                alcoholic_fermentation_state,
                current_volume_liters,
                current_material_state_id,
                initial_sugar_specific_gravity,
                initial_acidity_g_l_as_malic,
                version,
                created_at,
                updated_at
            ) VALUES (
                :batchId,
                :workspaceId,
                'Measurement production',
                :vesselId,
                'ACTIVE',
                'NOT_STARTED',
                100,
                :materialStateId,
                1.050,
                6.0,
                0,
                :createdAt,
                :updatedAt
            )
            """,
        materialSubjectParameters(subject, materialStateId)
    );
  }

  private @NonNull MapSqlParameterSource materialSubjectParameters(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId materialStateId
  ) {
    final var timestamp = OffsetDateTime.parse("2026-07-20T10:00:00Z");
    return new MapSqlParameterSource()
        .addValue("batchId", subject.id())
        .addValue("workspaceId", workspaceId.value())
        .addValue("vesselId", VesselTestFixtures.FEEDSTOCK_VESSEL_ID.value())
        .addValue("materialStateId", materialStateId.value())
        .addValue("createdAt", timestamp)
        .addValue("updatedAt", timestamp);
  }

  private void insertStateTransition(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId inputStateId,
      @NonNull MaterialStateId outputStateId
  ) {
    final var stepId = newUuid();
    final var actorId = newUuid();
    final var occurredAt = OffsetDateTime.parse("2026-07-20T11:00:00Z");
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
            ) VALUES (
                :stepId,
                :workspaceId,
                'PRODUCTION_BATCH_CREATED',
                :occurredAt,
                :performedByType,
                :performedById,
                :createdAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("stepId", stepId)
            .addValue("workspaceId", workspaceId.value())
            .addValue("performedByType", ActorType.MACHINE_IDENTITY.name())
            .addValue("performedById", actorId)
            .addValue("occurredAt", occurredAt)
            .addValue("createdAt", occurredAt)
    );
    insertStateTransitionSubject(stepId, subject, inputStateId, "INPUT", false);
    insertStateTransitionSubject(stepId, subject, outputStateId, "OUTPUT", true);
  }

  private void insertStateTransitionSubject(
      @NonNull UUID stepId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId stateId,
      @NonNull String role,
      boolean invalidatePh
  ) {
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
            ) VALUES (
                :stepId,
                :subjectRole,
                0,
                :traceStateId,
                :subjectType,
                :subjectId,
                CASE
                  WHEN :invalidatePh THEN ARRAY['PH']::VARCHAR[]
                  ELSE ARRAY[]::VARCHAR[]
                END
            )
            """,
        new MapSqlParameterSource()
            .addValue("stepId", stepId)
            .addValue("subjectRole", role)
            .addValue("traceStateId", stateId.value())
            .addValue("subjectType", subject.type().name())
            .addValue("subjectId", subject.id())
            .addValue("invalidatePh", invalidatePh)
    );
  }

  @Test
  void preservesEveryCharacteristicSpecificValueTypeAndValueShape() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        newUuid()
    );
    final var records = List.of(
        directResult(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050")
        ),
        directResult(
            MeasurementCharacteristic.PH,
            interval(MeasurementCharacteristic.PH, "3.20", "3.40")
        ),
        directResult(
            MeasurementCharacteristic.TITRATABLE_ACIDITY,
            point(MeasurementCharacteristic.TITRATABLE_ACIDITY, "6.0")
        ),
        directResult(
            MeasurementCharacteristic.TANNIN,
            point(MeasurementCharacteristic.TANNIN, "1200")
        ),
        directResult(
            MeasurementCharacteristic.YAN,
            point(MeasurementCharacteristic.YAN, "180")
        ),
        directResult(
            MeasurementCharacteristic.TEMPERATURE,
            point(MeasurementCharacteristic.TEMPERATURE, "18.5")
        ),
        directResult(
            MeasurementCharacteristic.TURBIDITY,
            point(MeasurementCharacteristic.TURBIDITY, "80")
        )
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, records)
    );

    repository.create(set);

    assertThat(repository.findVersionedById(set.measurementSetId()).orElseThrow()
        .aggregate()
        .currentRevision()
        .records())
        .containsExactlyInAnyOrderElementsOf(records);
  }

  @Test
  void combinesLatestCharacteristicsFromIndependentSetsOnTheSameMaterialState() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var sugar = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052"),
        RECORDED_AT.minusSeconds(3600)
    );
    final var ph = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.35"),
        RECORDED_AT
    );
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, stateId, List.of(sugar))
    ));
    initializeCurrentState(subject, stateId);
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, stateId, List.of(ph))
    ));

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, stateId)))
        .extracting(
            CurrentMeasurementResultView::characteristic,
            CurrentMeasurementResultView::recordId,
            CurrentMeasurementResultView::evidenceMaterialStateId
        )
        .containsExactlyInAnyOrder(
            tuple(MeasurementCharacteristic.SPECIFIC_GRAVITY, sugar.recordId(), stateId),
            tuple(MeasurementCharacteristic.PH, ph.recordId(), stateId)
        );
  }

  @Test
  void selectsCorrectedRevisionHeadWithoutChangingMaterialState() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var initialResult = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050")
    );
    final var initialRevision = revision(1, stateId, List.of(initialResult));
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        initialRevision
    );
    final var created = repository.create(set);
    initializeCurrentState(subject, stateId);
    final var correctedResult = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.055")
    );
    final var correctedRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        stateId,
        initialRevision.name(),
        null,
        MeasurementCorrectionReason.of("Corrected transcription error"),
        RECORDED_AT.plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        List.of(correctedResult),
        List.of()
    );

    set.correct(correctedRevision);
    repository.appendRevision(created.withAggregate(set));

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, stateId)))
        .singleElement()
        .returns(stateId, CurrentMeasurementResultView::currentMaterialStateId)
        .returns(stateId, CurrentMeasurementResultView::evidenceMaterialStateId)
        .returns(correctedResult.recordId(), CurrentMeasurementResultView::recordId)
        .satisfies(result -> assertThat(pointValue(result.value()).asBigDecimal())
            .isEqualByComparingTo("1.055"));
  }

  @Test
  void fullReplacementMakesEveryCorrectedResultCurrentOnTheObservedState() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var initialRevision = revision(
        1,
        stateId,
        List.of(
            directResult(
                MeasurementCharacteristic.SPECIFIC_GRAVITY,
                point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050")
            ),
            directResult(
                MeasurementCharacteristic.PH,
                point(MeasurementCharacteristic.PH, "3.40")
            )
        )
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        initialRevision
    );
    final var created = repository.create(set);
    initializeCurrentState(subject, stateId);
    final var correctedSugar = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.055")
    );
    final var correctedPh = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.25")
    );
    final var correctedRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        stateId,
        MeasurementSetName.of("Corrected laboratory panel"),
        null,
        MeasurementCorrectionReason.of("Corrected the complete laboratory report"),
        RECORDED_AT.plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        List.of(correctedSugar, correctedPh),
        List.of()
    );

    set.correct(correctedRevision);
    repository.appendRevision(created.withAggregate(set));

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, stateId)))
        .extracting(
            CurrentMeasurementResultView::characteristic,
            CurrentMeasurementResultView::recordId
        )
        .containsExactlyInAnyOrder(
            tuple(MeasurementCharacteristic.SPECIFIC_GRAVITY, correctedSugar.recordId()),
            tuple(MeasurementCharacteristic.PH, correctedPh.recordId())
        );
  }

  @Test
  void staleRevisionCannotAdvanceTheMeasurementSetHead() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var initialRevision = revision(
        1,
        stateId,
        List.of(directResult(
            MeasurementCharacteristic.PH,
            point(MeasurementCharacteristic.PH, "3.40")
        ))
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        initialRevision
    );
    final var staleState = repository.create(set);
    final var secondRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        stateId,
        initialRevision.name(),
        null,
        MeasurementCorrectionReason.of("First correction"),
        RECORDED_AT.plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        List.of(directResult(
            MeasurementCharacteristic.PH,
            point(MeasurementCharacteristic.PH, "3.35")
        )),
        List.of()
    );
    set.correct(secondRevision);
    repository.appendRevision(staleState.withAggregate(set));
    final var thirdRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(3),
        stateId,
        initialRevision.name(),
        null,
        MeasurementCorrectionReason.of("Competing correction"),
        RECORDED_AT.plusSeconds(120),
        ActorReference.accountUser(newUuid()),
        List.of(directResult(
            MeasurementCharacteristic.PH,
            point(MeasurementCharacteristic.PH, "3.30")
        )),
        List.of()
    );
    set.correct(thirdRevision);

    assertThatThrownBy(() -> repository.appendRevision(staleState.withAggregate(set)))
        .isInstanceOf(ConcurrentResourceModificationException.class);
    assertThat(repository.findVersionedById(set.measurementSetId()))
        .get()
        .returns(1L, persisted -> persisted.revision().value())
        .satisfies(persisted -> assertThat(persisted.aggregate().currentRevision())
            .returns(
                secondRevision.revisionId(),
                MeasurementSetRevision::revisionId
            ));
  }

  @Test
  void failedRevisionInsertRollsBackVersionAndHeadAdvance() {
    final var set = measurementSet(List.of(directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.40")
    )));
    final var state = repository.create(set);
    final var conflictingSet = measurementSet(List.of(directResult(
        MeasurementCharacteristic.TANNIN,
        point(MeasurementCharacteristic.TANNIN, "1200")
    )));
    repository.create(conflictingSet);
    final var forgedRevision = new MeasurementSetRevision(
        conflictingSet.currentRevision().revisionId(),
        set.currentRevision().revisionNumber().next(),
        set.currentRevision().materialStateId(),
        set.currentRevision().name(),
        set.currentRevision().note(),
        MeasurementCorrectionReason.of("Conflicting revision identity"),
        RECORDED_AT.plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        set.currentRevision().records(),
        set.currentRevision().resultRetractions()
    );
    final var forgedSet = MeasurementSet.restore(
        set.measurementSetId(),
        set.workspaceId(),
        set.subject(),
        forgedRevision,
        set.createdAt()
    );
    final var nestedTransaction = new TransactionTemplate(transactionManager);
    nestedTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);

    assertThatThrownBy(() -> nestedTransaction.executeWithoutResult(_ ->
        repository.appendRevision(state.withAggregate(forgedSet))
    ))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThat(repository.findVersionedById(set.measurementSetId()))
        .get()
        .returns(0L, persisted -> persisted.revision().value())
        .satisfies(persisted -> assertThat(persisted.aggregate().currentRevision())
            .returns(
                set.currentRevision().revisionId(),
                MeasurementSetRevision::revisionId
            ));
  }

  @Test
  void prefersEvidenceFromTheCurrentStateEvenWhenItsMeasurementTimeIsBackdated() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var previousStateId = MaterialStateId.newId();
    final var currentStateId = MaterialStateId.newId();
    final var newerInheritedResult = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052"),
        RECORDED_AT
    );
    final var backdatedCurrentResult = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.048"),
        RECORDED_AT.minusSeconds(7200)
    );
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, previousStateId, List.of(newerInheritedResult))
    ));
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, currentStateId, List.of(backdatedCurrentResult))
    ));
    insertFeedstockBatch(subject, currentStateId);
    insertStateTransition(subject, previousStateId, currentStateId);

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, currentStateId)))
        .singleElement()
        .returns(backdatedCurrentResult.recordId(), CurrentMeasurementResultView::recordId)
        .returns(currentStateId, CurrentMeasurementResultView::evidenceMaterialStateId);
  }

  @Test
  void doesNotLetABackdatedResultReplaceNewerEvidenceOnTheSameState() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var currentResult = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.40"),
        RECORDED_AT
    );
    final var backdatedResult = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.30"),
        RECORDED_AT.minusSeconds(3600)
    );
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, stateId, List.of(currentResult))
    ));
    initializeCurrentState(subject, stateId);
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, stateId, List.of(backdatedResult))
    ));

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, stateId)))
        .singleElement()
        .returns(currentResult.recordId(), CurrentMeasurementResultView::recordId);
  }

  @Test
  void excludesEvidenceInvalidatedByMaterialStateTransition() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var evidenceStateId = MaterialStateId.newId();
    final var currentStateId = MaterialStateId.newId();
    final var sugar = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052")
    );
    final var ph = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.35")
    );
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, evidenceStateId, List.of(sugar, ph))
    ));
    insertFeedstockBatch(subject, currentStateId);
    insertStateTransition(subject, evidenceStateId, currentStateId);

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, currentStateId)))
        .singleElement()
        .returns(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            CurrentMeasurementResultView::characteristic
        )
        .returns(evidenceStateId, CurrentMeasurementResultView::evidenceMaterialStateId);
  }

  @Test
  void acceptsNewEvidenceRecordedOnTheCurrentStateAfterInheritedEvidenceWasInvalidated() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var previousStateId = MaterialStateId.newId();
    final var currentStateId = MaterialStateId.newId();
    final var inheritedPh = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.35"),
        RECORDED_AT.minusSeconds(3600)
    );
    final var currentPh = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.25"),
        RECORDED_AT
    );
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, previousStateId, List.of(inheritedPh))
    ));
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, currentStateId, List.of(currentPh))
    ));
    insertFeedstockBatch(subject, currentStateId);
    insertStateTransition(subject, previousStateId, currentStateId);

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, currentStateId)))
        .singleElement()
        .returns(currentPh.recordId(), CurrentMeasurementResultView::recordId)
        .returns(currentStateId, CurrentMeasurementResultView::evidenceMaterialStateId);
  }

  @Test
  void retractionPreservesEvidenceAndRestoresPreviousApplicableResult() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var previousResult = new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.PH, "3.30"),
        RECORDED_AT.minusSeconds(3600),
        new MeasurementProvenance(
            MeasurementSourceType.USER,
            MeasurementSourceName.of("Manual entry"),
            null,
            null,
            null
        ),
        null,
        List.of()
    );
    final var previousStateId = MaterialStateId.newId();
    final var previousSet = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, previousStateId, List.of(previousResult))
    );
    repository.create(previousSet);
    initializeCurrentState(subject, previousStateId);

    final var currentResult = new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.PH, "3.40"),
        RECORDED_AT,
        new MeasurementProvenance(
            MeasurementSourceType.USER,
            MeasurementSourceName.of("Manual entry"),
            null,
            null,
            null
        ),
        null,
        List.of()
    );
    final var currentSet = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, previousStateId, List.of(currentResult))
    );
    final var created = repository.create(currentSet);

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, previousStateId)))
        .singleElement()
        .returns(currentResult.recordId(), CurrentMeasurementResultView::recordId);

    final var retractionActor = ActorReference.accountUser(newUuid());
    final var retractedAt = RECORDED_AT.plusSeconds(60);
    final var retractionReason = MeasurementRetractionReason.of("Invalidated measurement set");
    currentSet.retract(fullRetractionRevision(
        currentSet.currentRevision(),
        retractionActor,
        retractedAt,
        retractionReason
    ));
    repository.appendRevision(created);

    assertThat(viewQuery.findById(currentSet.measurementSetId()))
        .get()
        .returns(
            MeasurementSetLifecycle.RETRACTED,
            MeasurementSetView::lifecycle
        )
        .satisfies(view -> assertThat(view.retraction())
            .isNotNull()
            .returns(retractionActor, MeasurementSetRetractionView::actor)
            .returns(retractedAt, MeasurementSetRetractionView::retractedAt)
            .returns(retractionReason, MeasurementSetRetractionView::reason))
        .satisfies(view -> assertThat(view.records()).singleElement());
    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, previousStateId)))
        .singleElement()
        .returns(previousResult.recordId(), CurrentMeasurementResultView::recordId)
        .satisfies(result -> assertThat(pointValue(result.value()).asBigDecimal())
            .isEqualByComparingTo("3.30"));
  }

  @Test
  void retractingWholeMultiAxisSetRemovesAllItsResultsFromCurrentState() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var results = List.of(
        directResult(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052")
        ),
        directResult(
            MeasurementCharacteristic.PH,
            point(MeasurementCharacteristic.PH, "3.35")
        ),
        directResult(
            MeasurementCharacteristic.TITRATABLE_ACIDITY,
            point(MeasurementCharacteristic.TITRATABLE_ACIDITY, "6.20")
        )
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, stateId, results)
    );
    final var created = repository.create(set);
    initializeCurrentState(subject, stateId);

    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, stateId)))
        .extracting(CurrentMeasurementResultView::characteristic)
        .containsExactlyInAnyOrder(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            MeasurementCharacteristic.PH,
            MeasurementCharacteristic.TITRATABLE_ACIDITY
        );

    final var retractionActor = ActorReference.accountUser(newUuid());
    final var retractedAt = RECORDED_AT.plusSeconds(60);
    final var retractionReason = MeasurementRetractionReason.of("Invalidated measurement set");
    set.retract(fullRetractionRevision(
        set.currentRevision(),
        retractionActor,
        retractedAt,
        retractionReason
    ));
    repository.appendRevision(created);

    assertThat(viewQuery.findById(set.measurementSetId()))
        .get()
        .returns(MeasurementSetLifecycle.RETRACTED, MeasurementSetView::lifecycle)
        .satisfies(view -> assertThat(view.retraction())
            .isNotNull()
            .returns(retractionActor, MeasurementSetRetractionView::actor)
            .returns(retractedAt, MeasurementSetRetractionView::retractedAt)
            .returns(retractionReason, MeasurementSetRetractionView::reason))
        .satisfies(view -> assertThat(view.records()).hasSize(3));
    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, stateId))).isEmpty();
  }

  @Test
  void databaseHeadRejectsAnyRevisionAfterTerminalRetraction() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, List.of(directResult(
            MeasurementCharacteristic.PH,
            point(MeasurementCharacteristic.PH, "3.40")
        )))
    );
    final var created = repository.create(set);
    set.retract(fullRetractionRevision(
        set.currentRevision(),
        ActorReference.accountUser(newUuid()),
        RECORDED_AT.plusSeconds(60),
        MeasurementRetractionReason.of("Invalidated measurement set")
    ));
    final var retracted = repository.appendRevision(created);
    final var terminalRevision = set.currentRevision();
    final var forgedActiveRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        terminalRevision.revisionNumber().next(),
        terminalRevision.materialStateId(),
        terminalRevision.name(),
        terminalRevision.note(),
        MeasurementCorrectionReason.of("Invalid post-retraction correction"),
        RECORDED_AT.plusSeconds(120),
        ActorReference.accountUser(newUuid()),
        terminalRevision.records(),
        terminalRevision.resultRetractions()
    );
    final var forgedActiveSet = MeasurementSet.restore(
        set.measurementSetId(),
        set.workspaceId(),
        set.subject(),
        forgedActiveRevision,
        set.createdAt()
    );

    assertThatThrownBy(() -> repository.appendRevision(retracted.withAggregate(forgedActiveSet)))
        .isInstanceOf(ConcurrentResourceModificationException.class);
  }

  @Test
  void oneRevisionCanReplaceOneResultAndRetractAnother() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var stateId = MaterialStateId.newId();
    final var previousResult = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.30"),
        RECORDED_AT.minusSeconds(3_600)
    );
    repository.create(MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, stateId, List.of(previousResult))
    ));
    final var retractedResult = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.40"),
        RECORDED_AT
    );
    final var originalSugar = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052"),
        RECORDED_AT
    );
    final var initialRevision = revision(1, stateId, List.of(retractedResult, originalSugar));
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        initialRevision
    );
    final var created = repository.create(set);
    initializeCurrentState(subject, stateId);
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Laboratory invalidated the pH result");
    final var retractedAt = RECORDED_AT.plusSeconds(60);
    final var correctedSugar = directResult(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.049"),
        RECORDED_AT.plusSeconds(30)
    );
    final var correctedRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        stateId,
        initialRevision.name(),
        null,
        reason,
        retractedAt,
        actor,
        List.of(correctedSugar),
        List.of(new MeasurementResultRetraction(
            initialRevision.revisionId(),
            initialRevision.revisionNumber(),
            retractedResult,
            List.of(),
            actor,
            retractedAt,
            reason
        ))
    );

    set.correct(correctedRevision);
    repository.appendRevision(created.withAggregate(set));

    assertThat(repository.findVersionedById(set.measurementSetId()).orElseThrow()
        .aggregate()
        .currentRevision())
        .satisfies(revision -> assertThat(revision.records())
            .singleElement()
            .returns(
                MeasurementCharacteristic.SPECIFIC_GRAVITY,
                MeasurementRecord::characteristic
            ))
        .satisfies(revision -> assertThat(revision.resultRetractions())
            .singleElement()
            .returns(MeasurementCharacteristic.PH, MeasurementResultRetraction::characteristic)
            .returns(actor, MeasurementResultRetraction::actor)
            .returns(retractedAt, MeasurementResultRetraction::retractedAt)
            .returns(reason, MeasurementResultRetraction::reason));
    assertThat(viewQuery.findById(set.measurementSetId()))
        .get()
        .satisfies(view -> assertThat(view.records())
            .singleElement()
            .returns(
                MeasurementCharacteristic.SPECIFIC_GRAVITY,
                MeasurementRecordView::characteristic
            ))
        .satisfies(view -> assertThat(view.resultRetractions())
            .singleElement()
            .returns(retractedResult.recordId(), MeasurementResultRetractionView::recordId)
            .returns(MeasurementCharacteristic.PH, MeasurementResultRetractionView::characteristic)
            .returns(actor, MeasurementResultRetractionView::actor)
            .returns(retractedAt, MeasurementResultRetractionView::retractedAt)
            .returns(reason, MeasurementResultRetractionView::reason));
    assertThat(currentResultResolver.resolve(workspaceId, Map.of(subject, stateId)))
        .extracting(
            CurrentMeasurementResultView::characteristic,
            CurrentMeasurementResultView::recordId
        )
        .containsExactlyInAnyOrder(
            tuple(MeasurementCharacteristic.PH, previousResult.recordId()),
            tuple(MeasurementCharacteristic.SPECIFIC_GRAVITY, correctedSugar.recordId())
        );
  }

  @Test
  void preservesRepeatedMeasurementSnapshotWhenItsResultIsRetracted() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var firstObservation = observation("1.050", "2026-07-20T09:58:00Z");
    final var secondObservation = observation("1.052", "2026-07-20T10:00:00Z");
    final var representativeResult = result(firstObservation, secondObservation);
    final var initialRevision = revision(
        1,
        List.of(firstObservation, secondObservation, representativeResult)
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        initialRevision
    );
    final var created = repository.create(set);
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Hydrometer calibration was invalid");
    final var retractedAt = RECORDED_AT.plusSeconds(60);
    final var correction = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        initialRevision.materialStateId(),
        initialRevision.name(),
        null,
        reason,
        retractedAt,
        actor,
        List.of(),
        List.of(new MeasurementResultRetraction(
            initialRevision.revisionId(),
            initialRevision.revisionNumber(),
            representativeResult,
            List.of(firstObservation, secondObservation),
            actor,
            retractedAt,
            reason
        ))
    );

    set.correct(correction);
    final var retractedState = repository.appendRevision(created.withAggregate(set));
    final var carriedRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(3),
        initialRevision.materialStateId(),
        initialRevision.name(),
        "Reviewed retraction",
        MeasurementCorrectionReason.of("Added audit note"),
        retractedAt.plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        List.of(),
        correction.resultRetractions()
    );
    set.correct(carriedRevision);
    final var carriedState = repository.appendRevision(retractedState.withAggregate(set));
    final var fullRetractionActor = ActorReference.machineIdentity(newUuid());
    final var fullRetractionReason = MeasurementRetractionReason.of(
        "Laboratory withdrew the complete measurement set"
    );
    set.retract(fullRetractionRevision(
        set.currentRevision(),
        fullRetractionActor,
        retractedAt.plusSeconds(120),
        fullRetractionReason
    ));
    repository.appendRevision(carriedState.withAggregate(set));

    assertThat(repository.findVersionedById(set.measurementSetId()).orElseThrow()
        .aggregate()
        .currentRevision())
        .returns(MeasurementSetLifecycle.RETRACTED, MeasurementSetRevision::lifecycle)
        .returns(MeasurementSetRevisionChangeKind.RETRACTION, MeasurementSetRevision::changeKind)
        .returns(fullRetractionActor, MeasurementSetRevision::actor)
        .satisfies(revision -> assertThat(revision.resultRetractions())
            .singleElement()
            .returns(initialRevision.revisionId(), MeasurementResultRetraction::sourceRevisionId)
            .returns(initialRevision.revisionNumber(), MeasurementResultRetraction::sourceRevisionNumber)
            .returns(representativeResult, MeasurementResultRetraction::result)
            .returns(actor, MeasurementResultRetraction::actor)
            .returns(retractedAt, MeasurementResultRetraction::retractedAt)
            .returns(reason, MeasurementResultRetraction::reason)
            .returns(
                List.of(firstObservation, secondObservation),
                MeasurementResultRetraction::observations
            ));
    assertThat(viewQuery.findById(set.measurementSetId()).orElseThrow())
        .returns(MeasurementSetLifecycle.RETRACTED, MeasurementSetView::lifecycle)
        .returns(fullRetractionActor, MeasurementSetView::actor)
        .satisfies(view -> assertThat(view.resultRetractions())
            .singleElement()
            .returns(initialRevision.revisionId(), MeasurementResultRetractionView::sourceRevisionId)
            .returns(initialRevision.revisionNumber(), MeasurementResultRetractionView::sourceRevisionNumber)
            .returns(actor, MeasurementResultRetractionView::actor)
            .returns(retractedAt, MeasurementResultRetractionView::retractedAt)
            .returns(reason, MeasurementResultRetractionView::reason)
            .satisfies(retraction -> assertThat(retraction.resultSnapshot())
                .returns(representativeResult.recordId(), MeasurementRecordView::recordId)
                .returns(
                    MeasurementDerivationMethod.ARITHMETIC_MEAN,
                    MeasurementRecordView::derivationMethod
                )
                .returns(
                    List.of(firstObservation.recordId(), secondObservation.recordId()),
                    MeasurementRecordView::derivationInputs
                ))
            .satisfies(retraction -> assertThat(retraction.observationSnapshots())
                .extracting(MeasurementRecordView::recordId)
                .containsExactly(firstObservation.recordId(), secondObservation.recordId())));
    assertThat(viewQuery.findBySubject(workspaceId, subject))
        .singleElement()
        .satisfies(view -> assertThat(view.resultRetractions())
            .singleElement()
            .returns(representativeResult.recordId(), MeasurementResultRetractionView::recordId)
            .returns(actor, MeasurementResultRetractionView::actor)
            .returns(reason, MeasurementResultRetractionView::reason));
    assertThat(viewQuery.findRevisionsById(set.measurementSetId()))
        .hasSize(4)
        .allSatisfy(view -> assertThat(representativeResult(view).derivationInputs())
            .containsExactly(firstObservation.recordId(), secondObservation.recordId()));
  }

  @Test
  void revisionHistoryKeepsRetractionAfterTheCharacteristicIsReplaced() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var initialResult = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.50"),
        RECORDED_AT
    );
    final var initialRevision = revision(1, List.of(initialResult));
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        initialRevision
    );
    final var created = repository.create(set);
    final var retractionReason = MeasurementCorrectionReason.of("Invalid pH calibration");
    final var retractionTime = RECORDED_AT.plusSeconds(60);
    final var retractionActor = ActorReference.accountUser(newUuid());
    final var retractionRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        initialRevision.materialStateId(),
        initialRevision.name(),
        null,
        retractionReason,
        retractionTime,
        retractionActor,
        List.of(),
        List.of(new MeasurementResultRetraction(
            initialRevision.revisionId(),
            initialRevision.revisionNumber(),
            initialResult,
            List.of(),
            retractionActor,
            retractionTime,
            retractionReason
        ))
    );
    set.correct(retractionRevision);
    final var retracted = repository.appendRevision(created.withAggregate(set));
    final var replacementResult = directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.35"),
        retractionTime.plusSeconds(30)
    );
    final var replacementRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(3),
        initialRevision.materialStateId(),
        initialRevision.name(),
        null,
        MeasurementCorrectionReason.of("Recorded corrected pH result"),
        retractionTime.plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        List.of(replacementResult),
        List.of()
    );
    set.correct(replacementRevision);
    repository.appendRevision(retracted.withAggregate(set));

    assertThat(viewQuery.findRevisionsById(set.measurementSetId()))
        .hasSize(3)
        .satisfiesExactly(
            latest -> assertThat(latest)
                .returns(3, view -> view.revisionNumber().value())
                .satisfies(view -> assertThat(view.resultRetractions()).isEmpty()),
            retractedView -> assertThat(retractedView)
                .returns(2, view -> view.revisionNumber().value())
                .satisfies(view -> assertThat(view.resultRetractions())
                    .singleElement()
                    .returns(initialResult.recordId(), MeasurementResultRetractionView::recordId)
                    .returns(retractionReason, MeasurementResultRetractionView::reason)),
            initial -> assertThat(initial)
                .returns(1, view -> view.revisionNumber().value())
        );
  }

  @Test
  void translatesDuplicateRequestedIdentityToApplicationConflict() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var result = new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.PH, "3.40"),
        RECORDED_AT,
        new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null),
        null,
        List.of()
    );
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        subject,
        revision(1, List.of(result))
    );
    repository.create(set);

    assertThatThrownBy(() -> repository.create(set))
        .isInstanceOf(MeasurementSetAlreadyExistsException.class)
        .hasMessageContaining(set.measurementSetId().value().toString());
  }

  @Test
  void freshSchemaContainsOnlyGenericMeasurementStorage() {
    final var legacyTable = jdbcTemplate.query(
        "SELECT to_regclass('public.feedstock_measurements') AS table_name",
        new MapSqlParameterSource(),
        (resultSet, rowNumber) -> resultSet.getString("table_name")
    );
    final var statisticalColumns = jdbcTemplate.queryForObject(
        """
            SELECT COUNT(*)
              FROM information_schema.columns
             WHERE table_schema = 'public'
               AND (
                   column_name LIKE '%standard_deviation%'
                   OR column_name LIKE '%sample_size%'
               )
            """,
        new MapSqlParameterSource(),
        Long.class
    );
    final var separateRetractionTable = jdbcTemplate.query(
        "SELECT to_regclass('public.measurement_result_retractions') AS table_name",
        new MapSqlParameterSource(),
        (resultSet, rowNumber) -> resultSet.getString("table_name")
    );
    final var revisionRecordColumns = jdbcTemplate.queryForList(
        """
            SELECT column_name
              FROM information_schema.columns
             WHERE table_schema = 'public'
               AND table_name = 'measurement_revision_records'
            """,
        new MapSqlParameterSource(),
        String.class
    );
    final var measurementSetColumns = jdbcTemplate.queryForList(
        """
            SELECT column_name
              FROM information_schema.columns
             WHERE table_schema = 'public'
               AND table_name = 'measurement_sets'
            """,
        new MapSqlParameterSource(),
        String.class
    );
    final var measurementRevisionColumns = jdbcTemplate.queryForList(
        """
            SELECT column_name
              FROM information_schema.columns
             WHERE table_schema = 'public'
               AND table_name = 'measurement_set_revisions'
            """,
        new MapSqlParameterSource(),
        String.class
    );

    assertThat(legacyTable)
        .singleElement()
        .isNull();
    assertThat(separateRetractionTable)
        .singleElement()
        .isNull();
    assertThat(statisticalColumns).isZero();
    assertThat(revisionRecordColumns).contains(
        "measurement_set_id",
        "revision_id",
        "record_id",
        "characteristic",
        "role",
        "record_status"
    ).doesNotContain(
        "retraction_actor_reference",
        "retracted_at",
        "retraction_reason",
        "source_revision_id"
    );
    assertThat(measurementSetColumns).doesNotContain(
        "retracted_by",
        "retracted_at",
        "retraction_reason",
        "lifecycle",
        "updated_at"
    );
    assertThat(measurementRevisionColumns).contains(
        "lifecycle",
        "change_kind",
        "correction_reason",
        "retraction_reason",
        "performed_by_type",
        "performed_by_id"
    ).doesNotContain(
        "actor_reference"
    );
  }

  @Test
  void databaseRejectsRetractionRevisionWithoutAuditReason() {
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        revision(1, List.of(directResult(
            MeasurementCharacteristic.PH,
            point(MeasurementCharacteristic.PH, "3.40")
        )))
    );
    repository.create(set);

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            UPDATE measurement_set_revisions
               SET lifecycle = 'RETRACTED',
                   change_kind = 'RETRACTION'
             WHERE revision_id = :revisionId
            """,
        new MapSqlParameterSource("revisionId", set.currentRevision().revisionId().value())
    ))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void databaseRejectsUnsupportedMeasurementActorType() {
    final var set = measurementSet(List.of(directResult(
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.40")
    )));
    repository.create(set);

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            UPDATE measurement_set_revisions
               SET performed_by_type = 'UNSUPPORTED_ACTOR'
             WHERE revision_id = :revisionId
            """,
        new MapSqlParameterSource(
            "revisionId",
            set.currentRevision().revisionId().value()
        )
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("chk_measurement_revisions_actor_type");
  }

  @Test
  void databaseRejectsRetractionStatusForObservationEvidence() {
    final var firstObservation = observation("1.050", "2026-07-20T09:58:00Z");
    final var secondObservation = observation("1.052", "2026-07-20T10:00:00Z");
    final var representativeResult = result(firstObservation, secondObservation);
    final var set = MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        revision(1, List.of(firstObservation, secondObservation, representativeResult))
    );
    repository.create(set);

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            UPDATE measurement_revision_records
               SET record_status = 'RETRACTED'
             WHERE revision_id = :revisionId
               AND record_id = :recordId
            """,
        new MapSqlParameterSource()
            .addValue("revisionId", set.currentRevision().revisionId().value())
            .addValue("recordId", firstObservation.recordId().value())
    ))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void databaseRejectsSpecificGravityOutsideTheCanonicalRange() {
    final var measurementSetId = newUuid();
    final var revisionId = newUuid();
    final var subjectId = newUuid();
    jdbcTemplate.update(
        """
            INSERT INTO measurement_sets (
                measurement_set_id,
                workspace_id,
                subject_type,
                subject_id,
                version,
                created_at
            ) VALUES (
                :measurementSetId,
                :workspaceId,
                'PRODUCTION_BATCH',
                :subjectId,
                0,
                :createdAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId)
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectId", subjectId)
            .addValue("createdAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
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
            ) VALUES (
                :revisionId,
                :measurementSetId,
                1,
                :materialStateId,
                'ACTIVE',
                'INITIAL',
                'Invalid gravity evidence',
                :createdAt,
                'ACCOUNT_USER',
                :performedById
            )
            """,
        new MapSqlParameterSource()
            .addValue("revisionId", revisionId)
            .addValue("measurementSetId", measurementSetId)
            .addValue("materialStateId", newUuid())
            .addValue("createdAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
            .addValue("performedById", newUuid())
    );

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            INSERT INTO measurement_records (
                measurement_set_id,
                record_id,
                characteristic,
                role,
                value_kind,
                measured_at,
                source_type,
                point_value
            ) VALUES (
                :measurementSetId,
                :recordId,
                'SPECIFIC_GRAVITY',
                'RESULT',
                'POINT',
                :measuredAt,
                'USER',
                0.899
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId)
            .addValue("recordId", newUuid())
            .addValue("measuredAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("chk_measurement_records_specific_gravity_range");
  }

  @ParameterizedTest(name = "{0} rejects point value {1}")
  @CsvSource({
      "PH, -0.01, chk_measurement_records_ph_range",
      "PH, 15.01, chk_measurement_records_ph_range",
      "TITRATABLE_ACIDITY, -0.01, chk_measurement_records_titratable_acidity_range",
      "TITRATABLE_ACIDITY, 30.01, chk_measurement_records_titratable_acidity_range",
      "TANNIN, -1, chk_measurement_records_tannin_range",
      "TANNIN, 12001, chk_measurement_records_tannin_range",
      "YAN, -1, chk_measurement_records_yan_range",
      "YAN, 2001, chk_measurement_records_yan_range",
      "TEMPERATURE, -273.16, chk_measurement_records_temperature_range",
      "TURBIDITY, -0.01, chk_measurement_records_turbidity_range"
  })
  void databaseRejectsPointValuesOutsideCanonicalCharacteristicRange(
      String characteristic,
      BigDecimal invalidValue,
      String constraintName
  ) {
    final var measurementSetId = persistMeasurementSetIdentity();

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            INSERT INTO measurement_records (
                measurement_set_id,
                record_id,
                characteristic,
                role,
                value_kind,
                measured_at,
                source_type,
                point_value
            ) VALUES (
                :measurementSetId,
                :recordId,
                :characteristic,
                'RESULT',
                'POINT',
                :measuredAt,
                'USER',
                :pointValue
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId)
            .addValue("recordId", newUuid())
            .addValue("characteristic", characteristic)
            .addValue("measuredAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
            .addValue("pointValue", invalidValue)
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(constraintName);
  }

  @ParameterizedTest(name = "{0} rejects interval [{1}, {2}]")
  @CsvSource({
      "PH, -0.01, 7, chk_measurement_records_ph_range",
      "PH, 7, 15.01, chk_measurement_records_ph_range",
      "TITRATABLE_ACIDITY, -0.01, 7, chk_measurement_records_titratable_acidity_range",
      "TITRATABLE_ACIDITY, 7, 30.01, chk_measurement_records_titratable_acidity_range",
      "TANNIN, -1, 1000, chk_measurement_records_tannin_range",
      "TANNIN, 1000, 12001, chk_measurement_records_tannin_range",
      "YAN, -1, 100, chk_measurement_records_yan_range",
      "YAN, 100, 2001, chk_measurement_records_yan_range",
      "TEMPERATURE, -273.16, 20, chk_measurement_records_temperature_range",
      "TURBIDITY, -0.01, 100, chk_measurement_records_turbidity_range"
  })
  void databaseRejectsIntervalsOutsideCanonicalCharacteristicRange(
      String characteristic,
      BigDecimal lowerValue,
      BigDecimal upperValue,
      String constraintName
  ) {
    final var measurementSetId = persistMeasurementSetIdentity();

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            INSERT INTO measurement_records (
                measurement_set_id,
                record_id,
                characteristic,
                role,
                value_kind,
                measured_at,
                source_type,
                lower_value,
                upper_value
            ) VALUES (
                :measurementSetId,
                :recordId,
                :characteristic,
                'RESULT',
                'INTERVAL',
                :measuredAt,
                'USER',
                :lowerValue,
                :upperValue
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId)
            .addValue("recordId", newUuid())
            .addValue("characteristic", characteristic)
            .addValue("measuredAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
            .addValue("lowerValue", lowerValue)
            .addValue("upperValue", upperValue)
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(constraintName);
  }

  @Test
  void databaseRejectsCurrentRevisionOwnedByAnotherMeasurementSet() {
    final var firstSetId = newUuid();
    final var secondSetId = newUuid();
    final var secondRevisionId = newUuid();
    final var createdAt = OffsetDateTime.parse("2026-07-20T10:00:00Z");
    jdbcTemplate.update(
        """
            INSERT INTO measurement_sets (
                measurement_set_id,
                workspace_id,
                subject_type,
                subject_id,
                version,
                created_at
            ) VALUES
                (:firstSetId, :workspaceId, 'PRODUCTION_BATCH', :firstSubjectId, 0, :createdAt),
                (:secondSetId, :workspaceId, 'PRODUCTION_BATCH', :secondSubjectId, 0, :createdAt)
            """,
        new MapSqlParameterSource()
            .addValue("firstSetId", firstSetId)
            .addValue("secondSetId", secondSetId)
            .addValue("workspaceId", workspaceId.value())
            .addValue("firstSubjectId", newUuid())
            .addValue("secondSubjectId", newUuid())
            .addValue("createdAt", createdAt)
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
            ) VALUES (
                :revisionId,
                :measurementSetId,
                1,
                :materialStateId,
                'ACTIVE',
                'INITIAL',
                'Second set revision',
                :createdAt,
                'ACCOUNT_USER',
                :performedById
            )
            """,
        new MapSqlParameterSource()
            .addValue("revisionId", secondRevisionId)
            .addValue("measurementSetId", secondSetId)
            .addValue("materialStateId", newUuid())
            .addValue("createdAt", createdAt)
            .addValue("performedById", newUuid())
    );

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            INSERT INTO measurement_set_heads (
                measurement_set_id,
                revision_id
            ) VALUES (
                :measurementSetId,
                :revisionId
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", firstSetId)
            .addValue("revisionId", secondRevisionId)
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("fk_measurement_set_heads_revision");
  }

  private UUID persistMeasurementSetIdentity() {
    final var measurementSetId = newUuid();
    jdbcTemplate.update(
        """
            INSERT INTO measurement_sets (
                measurement_set_id,
                workspace_id,
                subject_type,
                subject_id,
                version,
                created_at
            ) VALUES (
                :measurementSetId,
                :workspaceId,
                'PRODUCTION_BATCH',
                :subjectId,
                0,
                :createdAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId)
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectId", newUuid())
            .addValue("createdAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
    );
    return measurementSetId;
  }

  private MeasurementSet measurementSet(List<MeasurementRecord> records) {
    return MeasurementSet.create(
        MeasurementSetId.newId(),
        workspaceId,
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        revision(1, records)
    );
  }

  private static MeasurementSetRevision revision(
      int number,
      List<MeasurementRecord> records
  ) {
    return revision(number, MaterialStateId.newId(), records);
  }

  private static @NonNull MeasurementRecordView representativeResult(
      @NonNull MeasurementSetView view
  ) {
    if (!view.resultRetractions().isEmpty()) {
      return view.resultRetractions().getFirst().resultSnapshot();
    }
    return view.records().stream()
        .filter(recordView -> recordView.role() == MeasurementRecordRole.RESULT)
        .findFirst()
        .orElseThrow();
  }

  private static MeasurementSetRevision revision(
      int number,
      MaterialStateId materialStateId,
      List<MeasurementRecord> records
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(number),
        materialStateId,
        MeasurementSetName.of("Repeated gravity reading"),
        null,
        number == 1 ? null : MeasurementCorrectionReason.of("Corrected result"),
        RECORDED_AT,
        ActorReference.accountUser(newUuid()),
        records,
        List.of()
    );
  }

  private static MeasurementSetRevision fullRetractionRevision(
      MeasurementSetRevision current,
      ActorReference actor,
      Instant retractedAt,
      MeasurementRetractionReason reason
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        current.revisionNumber().next(),
        current.materialStateId(),
        MeasurementSetLifecycle.RETRACTED,
        MeasurementSetRevisionChangeKind.RETRACTION,
        current.name(),
        current.note(),
        null,
        reason,
        retractedAt,
        actor,
        current.records(),
        current.resultRetractions()
    );
  }

  private static MeasurementRecord observation(
      String value,
      String measuredAt
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementRecordRole.OBSERVATION,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, value),
        Instant.parse(measuredAt),
        new MeasurementProvenance(
            MeasurementSourceType.INSTRUMENT,
            MeasurementSourceName.of("Hydrometer H-1"),
            MeasurementMethod.of("Hydrometer"),
            MeasurementSourceReference.of("instrument:H-1"),
            ConfidenceLevel.HIGH
        ),
        null,
        List.of()
    );
  }

  private static MeasurementRecord result(
      MeasurementRecord first,
      MeasurementRecord second
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.051"),
        second.measuredAt(),
        MeasurementProvenance.derivedFrom(List.of(first.provenance(), second.provenance())),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(first.recordId(), second.recordId())
    );
  }

  private static MeasurementRecord directResult(
      MeasurementCharacteristic characteristic,
      CharacteristicValue<MeasuredValue> value
  ) {
    return directResult(characteristic, value, RECORDED_AT);
  }

  private static MeasurementRecord directResult(
      MeasurementCharacteristic characteristic,
      CharacteristicValue<MeasuredValue> value,
      Instant measuredAt
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        characteristic,
        MeasurementRecordRole.RESULT,
        value,
        measuredAt,
        new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null),
        null,
        List.of()
    );
  }

  private static PointCharacteristicValue<MeasuredValue> point(
      MeasurementCharacteristic characteristic,
      String value
  ) {
    return PointCharacteristicValue.of(
        value(characteristic, new BigDecimal(value))
    );
  }

  private static IntervalCharacteristicValue<MeasuredValue> interval(
      MeasurementCharacteristic characteristic,
      String lower,
      String upper
  ) {
    return IntervalCharacteristicValue.of(
        value(characteristic, new BigDecimal(lower)),
        value(characteristic, new BigDecimal(upper))
    );
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan({
      "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement",
      "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability",
      "org.craft.mcs.cap.infrastructure.adapter.out.traceability.measurement"
  })
  static class MeasurementJdbcAdaptersTestConfiguration {

    @Bean
    BatchTraceabilityAccessControl batchTraceabilityAccessControl() {
      return mock(BatchTraceabilityAccessControl.class);
    }

    @Bean
    QueryMaterialStateLineageUseCase queryMaterialStateLineageUseCase(
        MaterialStateLineageStore lineageStore,
        BatchTraceabilityAccessControl accessControl
    ) {
      return new QueryMaterialStateLineageService(lineageStore, accessControl);
    }

    @Bean
    CurrentMeasurementResultResolver currentMeasurementResultResolver(
        MeasurementResultEvidenceQuery evidenceQuery,
        MeasurementMaterialStateLineageProvider lineageProvider
    ) {
      return new CurrentMeasurementResultResolver(evidenceQuery, lineageProvider);
    }
  }
}
