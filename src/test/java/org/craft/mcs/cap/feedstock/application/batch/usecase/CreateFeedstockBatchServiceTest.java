package org.craft.mcs.cap.feedstock.application.batch.usecase;

import java.time.LocalDate;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.CreateFeedstockBatchService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CreateFeedstockBatchServiceTest {

  @Test
  void createsFeedstockBatchWithOptionalSourceAndVessel() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var service = new CreateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper()
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    when(repository.create(any(FeedstockBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

    final var created = service.create(new CreateFeedstockBatchCommand(
        workspaceId,
        BatchName.of("Late season fruit"),
        null,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        VolumeValue.ofLiters("250.000"),
        "Pressed in two sessions"
    ));

    assertThat(created.batchId()).isNotNull();
    assertThat(created.workspaceId()).isEqualTo(workspaceId);
    assertThat(created.vessel()).isNull();
    assertThat(created.source()).isNull();
    assertThat(created.availableVolume().liters()).isEqualByComparingTo("250.000");
    assertThat(created.note()).isEqualTo("Pressed in two sessions");
  }

  @Test
  void rejectsCreatingFeedstockBatchWithZeroAvailableVolume() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var service = new CreateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper()
    );
    final var command = new CreateFeedstockBatchCommand(
        WorkspaceId.of(newUuid()),
        BatchName.of("Late season fruit"),
        null,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        VolumeValue.ofLiters("0"),
        null
    );

    assertThatThrownBy(() -> service.create(command))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("available volume must be greater than zero");
    verify(repository, never()).create(any(FeedstockBatch.class));
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
