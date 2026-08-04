package org.craft.mcs.cap.feedstock.domain.measurement;

import org.craft.mcs.cap.core.feedstock.domain.measurement.FeedstockSpecificGravityPolicy;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockSpecificGravityPolicyTest {

  private final FeedstockSpecificGravityPolicy policy = new FeedstockSpecificGravityPolicy();

  @Test
  void acceptsSugarBearingFeedstockSpecificGravity() {
    assertThatCode(() -> policy.validate(SpecificGravityValue.of("1.001")))
        .doesNotThrowAnyException();
  }

  @Test
  void rejectsSpecificGravityWithoutFermentableExtract() {
    assertThatThrownBy(() -> policy.validate(SpecificGravityValue.of("1.000")))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("greater than 1.000");

    assertThatThrownBy(() -> policy.validate(SpecificGravityValue.of("0.996")))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("greater than 1.000");
  }
}
