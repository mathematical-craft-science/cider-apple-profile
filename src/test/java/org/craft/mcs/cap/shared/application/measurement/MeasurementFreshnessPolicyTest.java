package org.craft.mcs.cap.shared.application.measurement;

import java.time.Duration;
import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MeasurementFreshnessPolicyTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-01T00:00:00Z");
  private static final MeasurementFreshnessPolicy POLICY =
      new MeasurementFreshnessPolicy(Duration.ofDays(14));

  @Test
  void keepsEvidenceCurrentBeforeThreshold() {
    assertThat(POLICY.status(MEASURED_AT, MEASURED_AT.plus(Duration.ofDays(13))))
        .isEqualTo(MeasurementFreshnessStatus.CURRENT);
  }

  @Test
  void marksEvidenceStaleAtThreshold() {
    assertThat(POLICY.status(MEASURED_AT, MEASURED_AT.plus(Duration.ofDays(14))))
        .isEqualTo(MeasurementFreshnessStatus.STALE);
  }

  @Test
  void rejectsNonPositiveThreshold() {
    assertThatThrownBy(() -> new MeasurementFreshnessPolicy(Duration.ZERO))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement stale duration must be positive");
  }
}
