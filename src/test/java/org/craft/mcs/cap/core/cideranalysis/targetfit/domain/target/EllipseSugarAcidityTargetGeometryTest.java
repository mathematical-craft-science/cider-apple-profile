package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.EllipseSugarAcidityGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


class EllipseSugarAcidityTargetGeometryTest {

  private final EllipseSugarAcidityTargetGeometry geometry = new EllipseSugarAcidityTargetGeometry(
      new EllipseSugarAcidityGeometry(
          SpecificGravityValue.of("1.060"),
          TitratableAcidityValue.of("6.0"),
          new BigDecimal("0.010"),
          new BigDecimal("2.0")
      )
  );

  @Test
  void keepsPointInsideTargetUnchanged() {
    final var point = point("1.060", "6.0");

    assertThat(geometry)
        .returns(SugarAcidityGeometryShape.ELLIPSE, EllipseSugarAcidityTargetGeometry::shape)
        .matches(candidate -> candidate.contains(point))
        .returns(point, candidate -> candidate.nearestPointTo(point, this::rawManhattanDistance));
  }

  @Test
  void projectsExternalPointOntoEllipseBoundary() {
    final var nearest = geometry.nearestPointTo(
        point("1.080", "6.0"),
        this::rawManhattanDistance
    );

    assertThat(nearest.specificGravity().asBigDecimal()).isCloseTo(
        new BigDecimal("1.070"),
        within(new BigDecimal("0.0000001"))
    );
    assertThat(nearest.titratableAcidity().asBigDecimal()).isCloseTo(
        new BigDecimal("6.0"),
        within(new BigDecimal("0.0000001"))
    );
    assertThat(geometry.contains(nearest)).isTrue();
  }

  private BigDecimal rawManhattanDistance(
      SugarAcidityGraphPoint source,
      SugarAcidityGraphPoint target
  ) {
    return source.specificGravity().asBigDecimal()
        .subtract(target.specificGravity().asBigDecimal())
        .abs()
        .add(
            source.titratableAcidity().asBigDecimal()
                .subtract(target.titratableAcidity().asBigDecimal())
                .abs()
        );
  }

  private static SugarAcidityGraphPoint point(
      String specificGravity,
      String titratableAcidity
  ) {
    return new SugarAcidityGraphPoint(
        SpecificGravityValue.of(specificGravity),
        TitratableAcidityValue.of(titratableAcidity)
    );
  }
}
