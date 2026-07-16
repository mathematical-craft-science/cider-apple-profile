package org.craft.mcs.cap.feedstock.application.batch.usecase;

import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.ListFeedstockBatchesService;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ListFeedstockBatchesServiceTest {

  @Test
  void filtersBatchesByOverlappingSeason() {
    final var repository = mock(FeedstockBatchSummaryQueryRepository.class);
    final var service = new ListFeedstockBatchesService(repository, mock(FeedstockAccessControl.class));
    final var workspaceId = WorkspaceId.of(newUuid());
    final var matching = batch(workspaceId, "Late", "2026-10-01", "2026-12-15");
    final var outside = batch(workspaceId, "Early", "2026-08-01", "2026-08-31");
    when(repository.findAllByWorkspaceId(workspaceId)).thenReturn(List.of(matching, outside));

    final var result = service.list(new ListFeedstockBatchesQuery(
        workspaceId,
        FeedstockSeason.of(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"))
    ));

    assertThat(result)
        .extracting(FeedstockBatchSummary::batchId)
        .containsExactly(matching.batchId());
  }

  private static FeedstockBatchSummary batch(
      WorkspaceId workspaceId,
      String name,
      String startsOn,
      String endsOn
  ) {
    return new FeedstockBatchSummary(
        FeedstockBatchId.newId(),
        workspaceId,
        FeedstockBatchName.of(name),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse(startsOn), LocalDate.parse(endsOn)),
        null,
        VolumeValue.ofLiters("250.000"),
        null
    );
  }
}
