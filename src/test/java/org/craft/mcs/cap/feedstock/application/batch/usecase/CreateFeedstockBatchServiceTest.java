package org.craft.mcs.cap.feedstock.application.batch.usecase;

import java.time.LocalDate;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.CreateFeedstockBatchService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CreateFeedstockBatchServiceTest {

  @Test
  void createsFeedstockBatchWithOptionalSource() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var transactionHelper = new TransactionHelper() {
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
    final var service = new CreateFeedstockBatchService(repository, transactionHelper);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    when(repository.save(any(FeedstockBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

    final var created = service.create(new CreateFeedstockBatchCommand(
        ciderMakerId,
        FeedstockBatchName.of("Late season fruit"),
        FeedstockType.APPLE_JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        VolumeValue.ofLiters("250.000"),
        "Pressed in two sessions"
    ));

    assertThat(created.batchId()).isNotNull();
    assertThat(created.ciderMakerId()).isEqualTo(ciderMakerId);
    assertThat(created.source()).isNull();
    assertThat(created.availableVolume().liters()).isEqualByComparingTo("250.000");
    assertThat(created.note()).isEqualTo("Pressed in two sessions");
  }
}
