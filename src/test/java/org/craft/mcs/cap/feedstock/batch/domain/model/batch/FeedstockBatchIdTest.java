package org.craft.mcs.cap.feedstock.batch.domain.model.batch;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class FeedstockBatchIdTest {

  @Test
  void createsTheSameIdentityFromUuidAndItsTextRepresentation() {
    final var value = newUuid();

    assertThat(FeedstockBatchId.of(value))
        .isEqualTo(FeedstockBatchId.of(value.toString()))
        .extracting(FeedstockBatchId::value)
        .isEqualTo(value);
  }
}
