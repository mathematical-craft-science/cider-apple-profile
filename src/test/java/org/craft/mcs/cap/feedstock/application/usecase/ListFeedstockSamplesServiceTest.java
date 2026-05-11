package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSummary;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.ListFeedstockSamplesService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ListFeedstockSamplesServiceTest {

  @Test
  void listsSampleSummariesByBatchId() {
    final var repository = mock(FeedstockSampleQueryRepository.class);
    final var service = new ListFeedstockSamplesService(repository);
    final var batchId = FeedstockBatchId.newId();
    final var summary = new FeedstockSampleSummary(
        FeedstockSampleId.newId(),
        CiderMakerId.of(randomUUID()),
        batchId,
        FeedstockSampleName.of("Late press sample"),
        FeedstockType.APPLE_JUICE,
        Instant.parse("2026-10-15T10:00:00Z"),
        "first lab result"
    );
    when(repository.findSummariesByBatchId(batchId)).thenReturn(List.of(summary));

    final var result = service.listByBatchId(batchId);

    assertThat(result).containsExactly(summary);
  }
}
