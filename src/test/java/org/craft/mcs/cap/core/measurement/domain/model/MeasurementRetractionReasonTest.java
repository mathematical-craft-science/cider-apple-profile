package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MeasurementRetractionReasonTest {

  @Test
  void normalizesValidReasonAndAcceptsMaximumLength() {
    assertThat(MeasurementRetractionReason.of("  Invalid laboratory report  "))
        .returns("Invalid laboratory report", MeasurementRetractionReason::value);
    assertThat(MeasurementRetractionReason.of("r".repeat(1_000)).value())
        .hasSize(1_000);
  }

  @Test
  void rejectsBlankAndExcessiveReason() {
    assertThatThrownBy(() -> MeasurementRetractionReason.of("   "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement retraction reason cannot be blank");
    assertThatThrownBy(() -> MeasurementRetractionReason.of("r".repeat(1_001)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement retraction reason length must not exceed 1000 characters");
  }
}
