package org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class RectangleSugarAcidityGeometryTest {

  private static final RectangleSugarAcidityGeometry GEOMETRY =
      new RectangleSugarAcidityGeometry(
          MeasuredValueRange.closed(
              SpecificGravityValue.of("1.050"),
              SpecificGravityValue.of("1.060")
          ),
          MeasuredValueRange.closed(
              TitratableAcidityValue.of("4.0"),
              TitratableAcidityValue.of("6.0")
          )
      );

  @Test
  void containsPointsInsideBothCoordinateRanges() {
    final var point = point("1.055", "5.0");

    assertThat(GEOMETRY)
        .returns(SugarAcidityGeometryShape.RECTANGLE, RectangleSugarAcidityGeometry::shape)
        .matches(candidate -> candidate.contains(point));
  }

  @Test
  void rejectsPointsOutsideEitherCoordinateRange() {
    assertThat(GEOMETRY)
        .doesNotMatch(candidate -> candidate.contains(point("1.049", "5.0")))
        .doesNotMatch(candidate -> candidate.contains(point("1.055", "6.1")));
  }

  @Test
  void projectsOutsidePointOntoRectangleClosure() {
    assertThat(GEOMETRY.nearestPointTo(point("1.070", "3.0")))
        .returns(SpecificGravityValue.of("1.060"), SugarAcidityGraphPoint::specificGravity)
        .returns(TitratableAcidityValue.of("4.0"), SugarAcidityGraphPoint::titratableAcidity);
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
