package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
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

  @Autowired
  private PlatformTransactionManager transactionManager;

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

  @Test
  void loadsAggregateForOptimisticMaterialConsumption() {
    final var workspaceId = persistWorkspace();
    final var batch = feedstockBatch(workspaceId);
    repository.save(FeedstockBatchJdbcMapper.fromDomain(batch));
    final var aggregateRepository = new JdbcFeedstockBatchRepository(repository);
    final var loaded = aggregateRepository.findVersionedById(batch.batchId());

    assertThat(loaded)
        .isNotNull()
        .get()
        .extracting(VersionedAggregate::aggregate)
        .returns(batch.batchId(), FeedstockBatch::batchId)
        .returns(batch.availableVolume(), FeedstockBatch::availableVolume);
  }

  @Test
  void aggregateAdapterRejectsStateLoadedBeforeAnotherUpdate() {
    final var aggregateRepository = new JdbcFeedstockBatchRepository(repository);
    final var created = aggregateRepository.create(feedstockBatch(persistWorkspace()));
    final var first = aggregateRepository.findVersionedById(created.batchId()).orElseThrow();
    final var stale = aggregateRepository.findVersionedById(created.batchId()).orElseThrow();

    first.aggregate().rename(BatchName.of("First update"));
    aggregateRepository.update(first);
    stale.aggregate().rename(BatchName.of("Stale update"));

    assertThatThrownBy(() -> aggregateRepository.update(stale))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: feedstock batch");
  }

  @Test
  void aggregateAdapterRejectsDeletionOfStateLoadedBeforeAnotherUpdate() {
    final var aggregateRepository = new JdbcFeedstockBatchRepository(repository);
    final var created = aggregateRepository.create(feedstockBatch(persistWorkspace()));
    final var current = aggregateRepository.findVersionedById(created.batchId()).orElseThrow();
    final var stale = aggregateRepository.findVersionedById(created.batchId()).orElseThrow();

    current.aggregate().rename(BatchName.of("Updated before delete"));
    aggregateRepository.update(current);

    assertThatThrownBy(() -> aggregateRepository.delete(stale))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: feedstock batch");
    assertThat(aggregateRepository.findById(created.batchId())).isPresent();
  }

  @Test
  void excludesSoftDeletedBatchFromAggregateReads() {
    final var aggregateRepository = new JdbcFeedstockBatchRepository(repository);
    final var created = aggregateRepository.create(feedstockBatch(persistWorkspace()));
    final var current = aggregateRepository.findVersionedById(created.batchId()).orElseThrow();

    aggregateRepository.delete(current);

    assertThat(aggregateRepository.findVersionedById(created.batchId())).isEmpty();
  }

  @Test
  void persistsDepletedLifecycleWhenEntireVolumeIsConsumed() {
    final var aggregateRepository = new JdbcFeedstockBatchRepository(repository);
    final var created = aggregateRepository.create(feedstockBatch(persistWorkspace()));
    final var state = aggregateRepository.findVersionedById(created.batchId()).orElseThrow();

    state.aggregate().consumeAvailableVolume(created.availableVolume());
    aggregateRepository.update(state);

    assertThat(aggregateRepository.findById(created.batchId()))
        .isPresent()
        .get()
        .returns(BatchLifecycle.DEPLETED, FeedstockBatch::lifecycle)
        .satisfies(batch -> assertThat(batch.availableVolume().liters()).isZero());
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
            VALUES (?, 'Optimistic locking workspace', 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value()
    );
    return workspaceId;
  }
}
