package org.craft.mcs.cap.feedstock.planning.application.candidate.contract;

import java.util.HashSet;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningBatchCriteria;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedstockPlanningBatchCriteriaTest {

  @Test
  void preservesAStableImmutableBatchSelection() {
    final var batchId = FeedstockBatchId.newId();
    final var source = new HashSet<>(Set.of(batchId));

    final var criteria = new FeedstockPlanningBatchCriteria(source);
    source.clear();

    assertThat(criteria.batchIds())
        .containsExactly(batchId)
        .isUnmodifiable();
  }

  @Test
  void rejectsEmptyBatchSelection() {
    assertThatThrownBy(() -> new FeedstockPlanningBatchCriteria(Set.of()))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("feedstock planning batch ids cannot be empty");
  }
}
