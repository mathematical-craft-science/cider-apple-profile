package org.craft.mcs.cap.core.profileanalysis.classification.domain.result;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationCharacteristic;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ClassificationResultTest {

  @Test
  void rejectsBlankProfileCode() {
    assertThatThrownBy(() -> ClassificationResult.<ExampleClass>unclassified(" "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("classification profile code cannot be blank");
  }

  @Test
  void createsConsistentUnclassifiedResult() {
    final var result = ClassificationResult.<ExampleClass>unclassified("EXAMPLE");

    assertThat(result)
        .returns(false, ClassificationResult::classified)
        .returns(null, ClassificationResult::appleClass)
        .returns(ClassificationConfidence.UNKNOWN, ClassificationResult::confidence)
        .returns(false, ClassificationResult::borderline)
        .returns(List.of(), ClassificationResult::borderlineEvidence);
  }

  @Test
  void derivesBorderlineStatusFromEvidence() {
    final var evidence = new BorderlineEvidence(
        ClassificationCharacteristic.TANNIN,
        BorderlineReason.CLOSE_TO_BOUNDARY
    );

    final var result = ClassificationResult.classified(
        "EXAMPLE",
        ExampleClass.A,
        ClassificationConfidence.LOW,
        List.of(evidence)
    );

    assertThat(result)
        .returns(true, ClassificationResult::classified)
        .returns(true, ClassificationResult::borderline)
        .satisfies(value -> assertThat(value.borderlineEvidence()).containsExactly(evidence));
  }

  private enum ExampleClass {
    A
  }
}
