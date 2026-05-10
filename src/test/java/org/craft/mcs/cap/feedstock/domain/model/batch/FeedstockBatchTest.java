package org.craft.mcs.cap.feedstock.domain.model.batch;

import java.time.LocalDate;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;


class FeedstockBatchTest {

  @Test
  void createsBatchWithOptionalSourceAndAvailableVolume() {
    final var batch = FeedstockBatch.create(
        FeedstockBatchId.newId(),
        CiderMakerId.of(randomUUID()),
        FeedstockBatchName.of("Late season fruit"),
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250.000")
    );

    assertThat(batch.source()).isNull();

    batch.changeSource(FeedstockSource.of("North orchard"));

    assertThat(batch.source()).isNotNull();
    assertThat(batch.source().value()).isEqualTo("North orchard");
    assertThat(batch.availableVolume().liters()).isEqualByComparingTo("250.000");
  }
}
