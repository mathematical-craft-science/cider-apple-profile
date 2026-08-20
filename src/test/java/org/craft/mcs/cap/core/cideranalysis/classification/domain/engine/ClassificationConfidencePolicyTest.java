package org.craft.mcs.cap.core.cideranalysis.classification.domain.engine;

import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.longashton.LongAshton4CiderAppleClass;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.BorderlineEvidence;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.BorderlineReason;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.ClassificationAssessment;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.ClassificationConfidence;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationRule;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ClassificationConfidencePolicyTest {

  private final ClassificationConfidencePolicy policy = new ClassificationConfidencePolicy();

  @Test
  void reportsOverlapWhenIntervalIncludesAnExclusiveClassificationBoundary() {
    final var range = ClassificationValueRange.lessThan("4.5");
    final var rule = ClassificationRule.of(
        LongAshton4CiderAppleClass.SWEET,
        null,
        range,
        null
    );
    final var input = new CiderAppleClassificationInput(
        null,
        IntervalCharacteristicValue.of(
            TitratableAcidityValue.of("4.0"),
            TitratableAcidityValue.of("4.5")
        ),
        null
    );

    assertThat(rule.matches(input, ClassificationIntervalPolicy.MIDPOINT_WITH_BOUNDARY_EVIDENCE))
        .isTrue();
    assertThat(policy.assess(
        rule,
        input,
        ClassificationIntervalPolicy.MIDPOINT_WITH_BOUNDARY_EVIDENCE
    ))
        .returns(ClassificationConfidence.LOW, ClassificationAssessment::confidence)
        .satisfies(assessment -> assertThat(assessment.evidence())
            .anySatisfy(evidence -> assertThat(evidence)
                .returns(
                    ClassificationCharacteristic.ACIDITY,
                    BorderlineEvidence::characteristic
                )
                .returns(
                    BorderlineReason.RANGE_OVERLAPS_BOUNDARY,
                    BorderlineEvidence::reason
                )));
  }
}
