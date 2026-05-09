package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.ListFeedstockBatchesService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.craft.mcs.cap.shared.domain.measurement.VolumeValue;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ListFeedstockBatchesServiceTest {

  @Test
  void filtersBatchesByOverlappingSeason() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var service = new ListFeedstockBatchesService(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var matching = batch(ciderMakerId, "Late", "2026-10-01", "2026-12-15");
    final var outside = batch(ciderMakerId, "Early", "2026-08-01", "2026-08-31");
    when(repository.findAllByCiderMakerId(ciderMakerId)).thenReturn(List.of(matching, outside));

    final var result = service.list(new ListFeedstockBatchesQuery(
        ciderMakerId,
        FeedstockSeason.of(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"))
    ));

    assertThat(result)
        .extracting(FeedstockBatchSummary::batchId)
        .containsExactly(matching.batchId());
  }

  private static FeedstockBatch batch(
      CiderMakerId ciderMakerId,
      String name,
      String startsOn,
      String endsOn
  ) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        ciderMakerId,
        FeedstockBatchName.of(name),
        FeedstockSeason.of(LocalDate.parse(startsOn), LocalDate.parse(endsOn)),
        VolumeValue.ofLiters("250.000")
    );
  }
}
