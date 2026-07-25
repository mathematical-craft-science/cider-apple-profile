package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetAlreadyExistsException;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMeasurementResultQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
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
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
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

    assertThat(currentResultQuery.findBySubjects(workspaceId, Set.of(subject)))
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

  private void initializeCurrentState(
      MeasurementSubjectReference subject,
      MaterialStateId materialStateId
  ) {
    jdbcTemplate.update(
        """
            INSERT INTO batch_trace_current_states (
                workspace_id,
                subject_type,
                subject_id,
                current_state_id,
                version,
                updated_at
            ) VALUES (
                :workspaceId,
                :subjectType,
                :subjectId,
                :currentStateId,
                0,
                :updatedAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectType", subject.type().name())
            .addValue("subjectId", subject.id())
            .addValue("currentStateId", materialStateId.value())
            .addValue("updatedAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
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

    assertThat(currentResultQuery.findBySubjects(workspaceId, Set.of(subject)))
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

    assertThat(currentResultQuery.findBySubjects(workspaceId, Set.of(subject)))
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
    assertThat(currentResultQuery.findBySubjects(workspaceId, Set.of(subject)))
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
                current_revision_id,
                version,
                created_at,
                updated_at
            ) VALUES (
                :measurementSetId,
                :workspaceId,
                'PRODUCTION_BATCH',
                :subjectId,
                'ACTIVE',
                :revisionId,
                0,
                :createdAt,
                :updatedAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("measurementSetId", measurementSetId)
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectId", subjectId)
            .addValue("revisionId", revisionId)
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
                revision_id,
                characteristic,
                role,
                value_kind,
                measured_at,
                source_type,
                point_value
            ) VALUES (
                :recordId,
                :revisionId,
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
            .addValue("revisionId", revisionId)
            .addValue("measuredAt", OffsetDateTime.parse("2026-07-20T10:00:00Z"))
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("chk_measurement_records_specific_gravity_range");
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
