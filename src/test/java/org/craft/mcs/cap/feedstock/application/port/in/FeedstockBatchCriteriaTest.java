package org.craft.mcs.cap.feedstock.application.port.in;

import java.util.HashSet;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockBatchCriteriaTest {

  @Test
  void copiesBatchIds() {
    final var first = FeedstockBatchId.of("00000000-0000-0000-0000-000000000001");
    final var second = FeedstockBatchId.of("00000000-0000-0000-0000-000000000002");
    final var ids = new HashSet<>(Set.of(first));
    final var criteria = new FeedstockBatchCriteria(ids);

    ids.add(second);

    assertThat(criteria.batchIds()).containsExactly(first);
  }

  @Test
  void rejectsEmptyBatchIds() {
    assertThatThrownBy(() -> new FeedstockBatchCriteria(Set.of()))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("cannot be empty");
  }
}
