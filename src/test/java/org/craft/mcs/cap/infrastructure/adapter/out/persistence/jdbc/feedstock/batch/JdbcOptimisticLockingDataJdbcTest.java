package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresDataJdbcTestConfiguration.class)
class JdbcOptimisticLockingDataJdbcTest {

  @Autowired
  private SpringDataFeedstockBatchJdbcRepository repository;

  @Test
  void translatesRealSpringDataJdbcOptimisticLockingConflict() {
    final var saved = repository.save(FeedstockBatchJdbcMapper.fromDomain(feedstockBatch()));
    final var batchId = (UUID) ReflectionTestUtils.getField(saved, "batchId");
    final var firstCopy = repository.findById(batchId).orElseThrow();
    final var staleCopy = repository.findById(batchId).orElseThrow();

    ReflectionTestUtils.setField(firstCopy, "name", "Updated by first transaction");
    repository.save(firstCopy);

    ReflectionTestUtils.setField(staleCopy, "name", "Updated by stale transaction");

    assertThatThrownBy(() -> translateOptimisticLocking("feedstock batch", () -> repository.save(staleCopy)))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .satisfies(exception -> assertThat((ConcurrentResourceModificationException) exception)
            .extracting(ConcurrentResourceModificationException::resourceName)
            .isEqualTo("feedstock batch"));
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
