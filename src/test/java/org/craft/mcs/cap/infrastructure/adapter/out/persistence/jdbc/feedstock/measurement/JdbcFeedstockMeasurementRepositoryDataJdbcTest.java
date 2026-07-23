package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.measurement;

import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcFeedstockMeasurementRepository.class
})
class JdbcFeedstockMeasurementRepositoryDataJdbcTest {

  @Autowired
  private JdbcAggregateTemplate aggregateTemplate;

  @Autowired
  private SpringDataFeedstockMeasurementJdbcRepository springDataMeasurementRepository;

  @Autowired
  private JdbcFeedstockMeasurementRepository repository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void savesExistingMeasurementAsUpdate() {
    final var batchId = saveBatchAndReturnId();
    final var measurement = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.of(batchId),
        FeedstockMeasurementName.of("Initial profile"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );

    repository.create(measurement);
    final var saved = repository.findVersionedById(measurement.measurementId()).orElseThrow();

    saved.aggregate().rename(FeedstockMeasurementName.of("Corrected profile"));
    saved.aggregate().changeChemicalProfile(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.4")))
        .build());

    final var updated = repository.update(saved).aggregate();

    assertThat(springDataMeasurementRepository.count()).isOne();
    assertThat(updated.name().value()).isEqualTo("Corrected profile");
    assertThat(updated.chemicalProfile().titratableAcidity()).isPresent();
  }

  @Test
  void rejectsMeasurementStateLoadedBeforeAnotherUpdate() {
    final var batchId = saveBatchAndReturnId();
    final var measurement = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.of(batchId),
        FeedstockMeasurementName.of("Initial profile"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
    repository.create(measurement);
    final var first = repository.findVersionedById(measurement.measurementId()).orElseThrow();
    final var stale = repository.findVersionedById(measurement.measurementId()).orElseThrow();

    first.aggregate().rename(FeedstockMeasurementName.of("First update"));
    repository.update(first);
    stale.aggregate().rename(FeedstockMeasurementName.of("Stale update"));

    assertThatThrownBy(() -> repository.update(stale))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: feedstock measurement");
  }

  @Test
  void rejectsMeasurementDeletionOfStateLoadedBeforeAnotherUpdate() {
    final var batchId = saveBatchAndReturnId();
    final var measurement = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.of(batchId),
        FeedstockMeasurementName.of("Initial profile"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
    repository.create(measurement);
    final var current = repository.findVersionedById(measurement.measurementId()).orElseThrow();
    final var stale = repository.findVersionedById(measurement.measurementId()).orElseThrow();

    current.aggregate().rename(FeedstockMeasurementName.of("Updated before delete"));
    repository.update(current);

    assertThatThrownBy(() -> repository.delete(stale))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: feedstock measurement");
    assertThat(repository.findById(measurement.measurementId())).isPresent();
  }

  private UUID saveBatchAndReturnId() {
    final var workspaceId = persistWorkspace();
    final var saved = aggregateTemplate.insert(FeedstockBatchJdbcMapper.fromDomain(feedstockBatch(workspaceId)));
    return (UUID) ReflectionTestUtils.getField(saved, "batchId");
  }

  private static FeedstockBatch feedstockBatch(WorkspaceId workspaceId) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        workspaceId,
        BatchName.of("Late juice"),
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250")
    );
  }

  private WorkspaceId persistWorkspace() {
    final var workspaceId = WorkspaceId.newId();
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, 'Measurement workspace', 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value()
    );
    return workspaceId;
  }
}
