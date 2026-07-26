package org.craft.mcs.cap.feedstock.application.batch.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.UpdateFeedstockBatchService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class UpdateFeedstockBatchServiceTest {

  @Test
  void updatesMetadataWithoutChangingMaterialStateProperties() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper()
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("250.000"));
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    final var updated = service.update(new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        BatchName.of("Renamed batch"),
        FeedstockType.BLEND,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-11-30")),
        null,
        false,
        "metadata update",
        false
    ));

    assertThat(updated)
        .returns(BatchName.of("Renamed batch"), FeedstockBatchSummary::name)
        .returns(FeedstockType.BLEND, FeedstockBatchSummary::type)
        .returns(Vessel.named("Tank A-01"), FeedstockBatchSummary::vessel)
        .returns(VolumeValue.ofLiters("250.000"), FeedstockBatchSummary::availableVolume);
    verify(repository).update(any());
  }

  @Test
  void allowsMetadataUpdateAfterBatchWasFullyConsumed() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper()
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("0"));
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    final var updated = service.update(new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        BatchName.of("Renamed batch"),
        null,
        null,
        null,
        false,
        null,
        false
    ));

    assertThat(updated)
        .returns(BatchName.of("Renamed batch"), FeedstockBatchSummary::name)
        .returns(VolumeValue.ofLiters("0"), FeedstockBatchSummary::availableVolume);
    verify(repository).update(any());
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
        availableVolume.isZero()
            ? BatchLifecycle.DEPLETED
            : BatchLifecycle.ACTIVE,
        availableVolume,
        MaterialStateId.newId(),
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
