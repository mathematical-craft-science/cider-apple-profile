package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.measurement;

import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;


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
        feedstockMeasurementInstant()
    );

    final var saved = repository.save(measurement);

    saved.rename(FeedstockMeasurementName.of("Corrected profile"));
    saved.changeChemicalProfile(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.4")))
        .build());

    final var updated = repository.save(saved);

    assertThat(springDataMeasurementRepository.count()).isOne();
    assertThat(updated.name().value()).isEqualTo("Corrected profile");
    assertThat(updated.chemicalProfile().titratableAcidity()).isPresent();
  }

  private UUID saveBatchAndReturnId() {
    final var saved = aggregateTemplate.insert(FeedstockBatchJdbcMapper.fromDomain(feedstockBatch()));
    return (UUID) ReflectionTestUtils.getField(saved, "batchId");
  }

  private static FeedstockBatch feedstockBatch() {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        CiderMakerId.of(UUID.randomUUID()),
        FeedstockBatchName.of("Late juice"),
        FeedstockType.APPLE_JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250")
    );
  }
}
