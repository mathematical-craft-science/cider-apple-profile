package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.ListFeedstockMeasurementsService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ListFeedstockMeasurementsServiceTest {

  @Test
  void listsSampleSummariesByBatchId() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = new ListFeedstockMeasurementsService(repository);
    final var batchId = FeedstockBatchId.newId();
    final var summary = new FeedstockMeasurementSummary(
        FeedstockMeasurementId.newId(),
        CiderMakerId.of(randomUUID()),
        batchId,
        FeedstockMeasurementName.of("Late press sample"),
        Instant.parse("2026-10-15T10:00:00Z"),
        "first lab result"
    );
    when(repository.findSummariesByBatchId(batchId)).thenReturn(List.of(summary));

    final var result = service.listByBatchId(batchId);

    assertThat(result).containsExactly(summary);
  }
}
