package org.craft.mcs.cap.feedstock.batch.application.batch.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.DeleteFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase.DeleteFeedstockBatchService;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;


class DeleteFeedstockBatchServiceTest {

  private static final @NonNull Instant DELETED_AT = Instant.parse("2026-08-09T12:00:00Z");

  @Test
  void softDeletesDepletedBatchThroughVersionedUpdate() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var accessControl = mock(FeedstockAccessControl.class);
    final var batch = activeBatch().consumeAvailableVolume(VolumeValue.ofLiters("250"));
    final var state = new VersionedAggregate<>(batch, AggregateRevision.of(3));
    final var updated = new AtomicReference<VersionedAggregate<FeedstockBatch>>();
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(state));
    when(repository.update(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
      final VersionedAggregate<FeedstockBatch> value = invocation.getArgument(0);
      updated.set(value);
      return value;
    });
    final var service = service(repository, accessControl);

    service.delete(new DeleteFeedstockBatchCommand(batch.batchId(), batch.workspaceId()));

    verify(accessControl).requireWriteAccess(batch.workspaceId());
    verify(repository).update(org.mockito.ArgumentMatchers.any());
    assertThat(updated.get())
        .returns(AggregateRevision.of(3), VersionedAggregate::revision)
        .extracting(VersionedAggregate::aggregate)
        .returns(true, FeedstockBatch::deleted)
        .returns(DELETED_AT, FeedstockBatch::deletedAt);
  }

  @Test
  void keepsActiveMaterialVisibleWhenDeletionIsRejected() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var batch = activeBatch();
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(
        new VersionedAggregate<>(batch, AggregateRevision.of(1))
    ));
    final var service = service(repository, mock(FeedstockAccessControl.class));

    final var command = new DeleteFeedstockBatchCommand(batch.batchId(), batch.workspaceId());
    assertThatThrownBy(() -> service.delete(command))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("cannot delete feedstock batch containing active material");
    verify(repository, never()).update(org.mockito.ArgumentMatchers.any());
  }

  private static @NonNull DeleteFeedstockBatchService service(
      @NonNull FeedstockBatchRepository repository,
      @NonNull FeedstockAccessControl accessControl
  ) {
    return new DeleteFeedstockBatchService(
        repository,
        accessControl,
        transactionHelper(),
        Clock.fixed(DELETED_AT, ZoneOffset.UTC)
    );
  }

  private static @NonNull FeedstockBatch activeBatch() {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Late season fruit"),
        VesselTestFixtures.FEEDSTOCK_VESSEL_ID,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250")
    );
  }

  private static @NonNull TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @Override
      public <T> @NonNull T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
        return supplier.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable runnable) {
        runnable.run();
      }
    };
  }
}
