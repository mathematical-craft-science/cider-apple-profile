package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetAlreadyExistsException;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMeasurementResultQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceReference;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.pointValue;


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
  private CurrentMeasurementResultQuery currentResultQuery;

  @Autowired
  private MeasurementSetViewQuery viewQuery;

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

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
        .satisfies(measurementRecord -> assertThat(pointValue(measurementRecord.value()))
            .isInstanceOfSatisfying(
                SpecificGravityValue.class, value -> assertThat(value)
                    .returns(BeverageUnits.SG, SpecificGravityValue::unit)
                    .satisfies(specificGravity -> assertThat(specificGravity.asBigDecimal())
                        .isEqualByComparingTo("1.051"))
            ));

    assertThat(currentResultQuery.findByStates(
        workspaceId,
        Map.of(subject, set.currentRevision().materialStateId())
    ))
        .singleElement()
        .returns(result.recordId(), CurrentMeasurementResultView::recordId)
        .returns(MeasurementCharacteristic.SPECIFIC_GRAVITY, CurrentMeasurementResultView::characteristic)
        .satisfies(current -> assertThat(current.provenance())
            .returns(MeasurementSourceType.DERIVED, MeasurementProvenance::sourceType));
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
        initialRevision.records()
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

  private void initializeCurrentState(
      MeasurementSubjectReference subject,
      MaterialStateId materialStateId
  ) {
    switch (subject.type()) {
      case FEEDSTOCK_BATCH -> insertFeedstockBatch(subject, materialStateId);
      case PRODUCTION_BATCH -> insertProductionBatch(subject, materialStateId);
    }
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
                vessel,
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
                'Test vessel',
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
                vessel,
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
                'Test vessel',
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
                'SYSTEM',
                'test',
                :createdAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("stepId", stepId)
            .addValue("workspaceId", workspaceId.value())
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

    assertThat(currentResultQuery.findByStates(workspaceId, Map.of(subject, stateId)))
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

    assertThat(currentResultQuery.findByStates(workspaceId, Map.of(subject, currentStateId)))
        .singleElement()
        .returns(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            CurrentMeasurementResultView::characteristic
        )
        .returns(evidenceStateId, CurrentMeasurementResultView::evidenceMaterialStateId);
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

    assertThat(currentResultQuery.findByStates(workspaceId, Map.of(subject, previousStateId)))
        .singleElement()
        .returns(currentResult.recordId(), CurrentMeasurementResultView::recordId);

    currentSet.retract(RECORDED_AT.plusSeconds(60));
    repository.retract(created);

    assertThat(viewQuery.findById(currentSet.measurementSetId()))
        .get()
        .returns(
            MeasurementSetLifecycle.RETRACTED,
            MeasurementSetView::lifecycle
        )
        .satisfies(view -> assertThat(view.records()).singleElement());
    assertThat(currentResultQuery.findByStates(workspaceId, Map.of(subject, previousStateId)))
        .singleElement()
        .returns(previousResult.recordId(), CurrentMeasurementResultView::recordId)
        .satisfies(result -> assertThat(pointValue(result.value()).asBigDecimal())
            .isEqualByComparingTo("3.30"));
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

    assertThat(legacyTable)
        .singleElement()
        .isNull();
    assertThat(statisticalColumns).isZero();
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
                lifecycle,
                version,
                created_at,
                updated_at
            ) VALUES (
                :measurementSetId,
                :workspaceId,
                'PRODUCTION_BATCH',
                :subjectId,
                'ACTIVE',
                0,
                :createdAt,
                :updatedAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId)
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectId", subjectId)
            .addValue("createdAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
            .addValue("updatedAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
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
            ) VALUES (
                :revisionId,
                :measurementSetId,
                1,
                :materialStateId,
                'Invalid gravity evidence',
                :createdAt,
                :actorReference
            )
            """,
        new MapSqlParameterSource()
            .addValue("revisionId", revisionId)
            .addValue("measurementSetId", measurementSetId)
            .addValue("materialStateId", newUuid())
            .addValue("createdAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
            .addValue("actorReference", "ACCOUNT_USER:" + newUuid())
    );

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            INSERT INTO measurement_records (
                record_id,
                characteristic,
                role,
                value_kind,
                measured_at,
                source_type,
                point_value
            ) VALUES (
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
            .addValue("recordId", newUuid())
            .addValue("measuredAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("chk_measurement_records_specific_gravity_range");
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
                lifecycle,
                version,
                created_at,
                updated_at
            ) VALUES
                (:firstSetId, :workspaceId, 'PRODUCTION_BATCH', :firstSubjectId, 'ACTIVE', 0, :createdAt, :createdAt),
                (:secondSetId, :workspaceId, 'PRODUCTION_BATCH', :secondSubjectId, 'ACTIVE', 0, :createdAt, :createdAt)
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
                name,
                created_at,
                actor_reference
            ) VALUES (
                :revisionId,
                :measurementSetId,
                1,
                :materialStateId,
                'Second set revision',
                :createdAt,
                :actorReference
            )
            """,
        new MapSqlParameterSource()
            .addValue("revisionId", secondRevisionId)
            .addValue("measurementSetId", secondSetId)
            .addValue("materialStateId", newUuid())
            .addValue("createdAt", createdAt)
            .addValue("actorReference", "ACCOUNT_USER:" + newUuid())
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

  private static MeasurementSetRevision revision(
      int number,
      List<MeasurementRecord> records
  ) {
    return revision(number, MaterialStateId.newId(), records);
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
        null,
        RECORDED_AT,
        ActorReference.accountUser(newUuid()),
        records
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
        MeasurementProvenance.derived(),
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
  @ComponentScan("org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement")
  static class MeasurementJdbcAdaptersTestConfiguration {
  }
}
