package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresDataJdbcTestConfiguration.class)
class JdbcOptimisticLockingDataJdbcTest {

  @Autowired
  private SpringDataFeedstockBatchJdbcRepository repository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void translatesRealSpringDataJdbcOptimisticLockingConflict() {
    final var workspaceId = persistWorkspace();
    final var saved = repository.save(FeedstockBatchJdbcMapper.fromDomain(feedstockBatch(workspaceId)));
    final var batchId = (UUID) ReflectionTestUtils.getField(saved, "batchId");
    final var firstCopy = repository.findById(batchId).orElseThrow();
    final var staleCopy = repository.findById(batchId).orElseThrow();

    ReflectionTestUtils.setField(firstCopy, "name", "Updated by first transaction");
    repository.save(firstCopy);

    ReflectionTestUtils.setField(staleCopy, "name", "Updated by stale transaction");

    assertThatThrownBy(() -> JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "feedstock batch",
        () -> repository.save(staleCopy)
    ))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .satisfies(exception -> assertThat((ConcurrentResourceModificationException) exception)
            .extracting(ConcurrentResourceModificationException::resourceName)
            .isEqualTo("feedstock batch"));
  }

  private static FeedstockBatch feedstockBatch(WorkspaceId workspaceId) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        workspaceId,
        FeedstockBatchName.of("Late juice"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250")
    );
  }

  private WorkspaceId persistWorkspace() {
    final var workspaceId = WorkspaceId.of(UUID.randomUUID());
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, 'Optimistic locking workspace', 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value()
    );
    return workspaceId;
  }
}
