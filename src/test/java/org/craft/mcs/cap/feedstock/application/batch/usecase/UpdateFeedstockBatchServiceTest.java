package org.craft.mcs.cap.feedstock.application.batch.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.UpdateFeedstockBatchService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;


class UpdateFeedstockBatchServiceTest {

  @Test
  void rejectsUpdatingFeedstockBatchAvailableVolumeToZero() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper()
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("250.000"));
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));

    assertThatThrownBy(() -> service.update(new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        null,
        null,
        false,
        null,
        null,
        null,
        false,
        VolumeValue.ofLiters("0"),
        null,
        false
    )))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("available volume must be greater than zero");
    verify(repository, never()).update(any());
  }

  @Test
  void rejectsSavingEditedLegacyFeedstockBatchWithZeroAvailableVolume() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper()
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("0"));
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));

    assertThatThrownBy(() -> service.update(new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        BatchName.of("Renamed batch"),
        null,
        false,
        null,
        null,
        null,
        false,
        null,
        null,
        false
    )))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("available volume must be greater than zero");
    verify(repository, never()).update(any());
  }

  private static @NonNull FeedstockBatch batch(
      @NonNull WorkspaceId workspaceId,
      @NonNull VolumeValue availableVolume
  ) {
    return FeedstockBatch.restore(
        FeedstockBatchId.newId(),
        workspaceId,
        BatchName.of("Late season fruit"),
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        availableVolume,
        null,
        null
    );
  }

  private static @NonNull TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @NonNull
      @Override
      public <T> T callInTransactionNonNull(@NonNull Supplier<T> action) {
        return action.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable action) {
        action.run();
      }
    };
  }
}
