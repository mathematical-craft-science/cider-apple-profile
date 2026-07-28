package org.craft.mcs.cap.feedstock.domain.model.batch;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockSourceTest {

  @Test
  void normalizesOperationalSourceDescription() {
    assertThat(FeedstockSource.of("  North orchard  "))
        .extracting(FeedstockSource::value)
        .isEqualTo("North orchard");
  }

  @Test
  void rejectsBlankSourceDescription() {
    assertThatThrownBy(() -> FeedstockSource.of("   "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("feedstock source cannot be blank");
  }
}
