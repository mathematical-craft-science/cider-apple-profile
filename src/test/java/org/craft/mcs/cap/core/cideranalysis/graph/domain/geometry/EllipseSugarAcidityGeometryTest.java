package org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class EllipseSugarAcidityGeometryTest {

  private static final SugarAcidityGraphPoint CENTER = new SugarAcidityGraphPoint(
      SpecificGravityValue.of("1.060"),
      TitratableAcidityValue.of("6.0")
  );

  @Test
  void recognizesPointsInsideAndOutsideEllipse() {
    final var geometry = geometry(new BigDecimal("0.010"), new BigDecimal("2.0"));

    assertThat(geometry)
        .returns(SugarAcidityGeometryShape.ELLIPSE, EllipseSugarAcidityGeometry::shape)
        .matches(candidate -> candidate.contains(CENTER))
        .matches(candidate -> candidate.contains(new SugarAcidityGraphPoint(
            SpecificGravityValue.of("1.070"),
            TitratableAcidityValue.of("6.0")
        )))
        .matches(candidate -> !candidate.contains(new SugarAcidityGraphPoint(
            SpecificGravityValue.of("1.071"),
            TitratableAcidityValue.of("6.0")
        )));
  }

  @Test
  void rejectsNonPositiveSpecificGravityRadius() {
    assertThatThrownBy(() -> geometry(BigDecimal.ZERO, BigDecimal.ONE))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("ellipse specific gravity radius must be positive");
  }

  @Test
  void rejectsNonPositiveTitratableAcidityRadius() {
    assertThatThrownBy(() -> geometry(BigDecimal.ONE, BigDecimal.ZERO))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("ellipse titratable acidity radius must be positive");
  }

  private static EllipseSugarAcidityGeometry geometry(
      BigDecimal specificGravityRadius,
      BigDecimal titratableAcidityRadius
  ) {
    return new EllipseSugarAcidityGeometry(
        CENTER.specificGravity(),
        CENTER.titratableAcidity(),
        specificGravityRadius,
        titratableAcidityRadius
    );
  }
}
