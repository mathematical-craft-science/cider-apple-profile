package org.craft.mcs.cap.feedstock.domain.model.batch;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.time.LocalDate;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockSeasonTest {

  @Test
  void containsDatesInsideInclusiveRange() {
    final var season = FeedstockSeason.of(
        LocalDate.parse("2026-09-15"),
        LocalDate.parse("2026-12-15")
    );

    assertThat(season.contains(LocalDate.parse("2026-09-15"))).isTrue();
    assertThat(season.contains(LocalDate.parse("2026-10-10"))).isTrue();
    assertThat(season.contains(LocalDate.parse("2026-12-15"))).isTrue();
    assertThat(season.contains(LocalDate.parse("2026-12-16"))).isFalse();
  }

  @Test
  void rejectsEndDateBeforeStartDate() {
    assertThatThrownBy(() -> FeedstockSeason.of(
        LocalDate.parse("2026-12-15"),
        LocalDate.parse("2026-09-15")
    )).isInstanceOf(DomainRuleViolationException.class);
  }
}
