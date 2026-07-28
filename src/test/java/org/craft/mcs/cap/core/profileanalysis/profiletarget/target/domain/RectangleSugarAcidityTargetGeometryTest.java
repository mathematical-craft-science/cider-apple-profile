package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityPointDistance;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class RectangleSugarAcidityTargetGeometryTest {

  private static final SugarAcidityPointDistance UNUSED_DISTANCE =
      (source, target) -> BigDecimal.ZERO;

  private final RectangleSugarAcidityTargetGeometry geometry =
      new RectangleSugarAcidityTargetGeometry(
          MeasuredValueRange.closed(
              SpecificGravityValue.of("1.060"),
              SpecificGravityValue.of("1.080")
          ),
          MeasuredValueRange.closed(
              TitratableAcidityValue.of("4.5"),
              TitratableAcidityValue.of("7.5")
          )
      );

  @Test
  void containsPointInsideInclusiveBoundaries() {
    assertThat(geometry.contains(new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.060"),
        TitratableAcidityValue.of("4.5")
    ))).isTrue();
    assertThat(geometry.contains(new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.080"),
        TitratableAcidityValue.of("7.5")
    ))).isTrue();
  }

  @Test
  void rejectsPointOutsideRectangle() {
    assertThat(geometry.contains(new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.059"),
        TitratableAcidityValue.of("5.0")
    ))).isFalse();
    assertThat(geometry.contains(new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.070"),
        TitratableAcidityValue.of("7.6")
    ))).isFalse();
  }

  @Test
  void returnsNearestBoundaryPoint() {
    final var nearest = geometry.nearestPointTo(
        new SugarAcidityGraphPoint(
            SpecificGravityValue.of("1.050"),
            TitratableAcidityValue.of("8.5")
        ),
        UNUSED_DISTANCE
    );

    assertThat(nearest)
        .returns(
            SpecificGravityValue.of("1.060"),
            SugarAcidityGraphPoint::specificGravity
        )
        .returns(
            TitratableAcidityValue.of("7.5"),
            SugarAcidityGraphPoint::titratableAcidity
        );
  }

  @Test
  void returnsSamePointWhenAlreadyInside() {
    final var point = new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.070"),
        TitratableAcidityValue.of("6.0")
    );

    assertThat(geometry.nearestPointTo(point, UNUSED_DISTANCE)).isEqualTo(point);
  }

  @Test
  void preservesHalfOpenZoneBoundaryForPredefinedTarget() {
    final var halfOpenGeometry = new RectangleSugarAcidityTargetGeometry(
        MeasuredValueRange.closedOpen(
            SpecificGravityValue.of("1.060"),
            SpecificGravityValue.of("1.080")
        ),
        MeasuredValueRange.closedOpen(
            TitratableAcidityValue.of("4.5"),
            TitratableAcidityValue.of("7.5")
        )
    );
    final var upperBoundary = new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.070"),
        TitratableAcidityValue.of("7.5")
    );

    assertThat(halfOpenGeometry.contains(upperBoundary)).isFalse();
    assertThat(halfOpenGeometry.nearestPointTo(upperBoundary, UNUSED_DISTANCE))
        .isEqualTo(upperBoundary);
  }

  @Test
  void rejectsMissingRanges() {
    assertThatThrownBy(() -> new RectangleSugarAcidityTargetGeometry(
        null,
        MeasuredValueRange.closed(
            TitratableAcidityValue.of("4.5"),
            TitratableAcidityValue.of("7.5")
        )
    )).isInstanceOf(NullPointerException.class);
  }
}
