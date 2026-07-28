package org.craft.mcs.cap.production.domain.model;

import org.craft.mcs.cap.core.production.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProductionSourceEvidenceReferenceTest {

  @Test
  void normalizesAValidEvidenceReference() {
    assertThat(ProductionSourceEvidenceReference.of(" measurement-set-7 "))
        .extracting(ProductionSourceEvidenceReference::value)
        .isEqualTo("measurement-set-7");
  }

  @Test
  void rejectsAReferenceWithoutContent() {
    assertThatThrownBy(() -> ProductionSourceEvidenceReference.of("  "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source evidence reference cannot be blank");
  }

  @Test
  void rejectsAReferenceLongerThanThePersistenceBoundary() {
    final var tooLong = "x".repeat(161);

    assertThatThrownBy(() -> ProductionSourceEvidenceReference.of(tooLong))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source evidence reference length must not exceed 160");
  }
}
