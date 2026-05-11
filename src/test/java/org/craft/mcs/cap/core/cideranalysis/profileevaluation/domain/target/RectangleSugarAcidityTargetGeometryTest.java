package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class RectangleSugarAcidityTargetGeometryTest {

  private final RectangleSugarAcidityTargetGeometry geometry = new RectangleSugarAcidityTargetGeometry(
      new BigDecimal("1.060"),
      new BigDecimal("1.080"),
      new BigDecimal("4.5"),
      new BigDecimal("7.5")
  );

  @Test
  void containsPointInsideInclusiveBoundaries() {
    assertThat(geometry.contains(new SugarAcidityCoordinates(
        new BigDecimal("1.060"),
        new BigDecimal("4.5")
    ))).isTrue();
    assertThat(geometry.contains(new SugarAcidityCoordinates(
        new BigDecimal("1.080"),
        new BigDecimal("7.5")
    ))).isTrue();
  }

  @Test
  void rejectsPointOutsideRectangle() {
    assertThat(geometry.contains(new SugarAcidityCoordinates(
        new BigDecimal("1.059"),
        new BigDecimal("5.0")
    ))).isFalse();
    assertThat(geometry.contains(new SugarAcidityCoordinates(
        new BigDecimal("1.070"),
        new BigDecimal("7.6")
    ))).isFalse();
  }

  @Test
  void returnsNearestBoundaryPoint() {
    final var nearest = geometry.nearestPointTo(new SugarAcidityCoordinates(
        new BigDecimal("1.050"),
        new BigDecimal("8.5")
    ));

    assertThat(nearest.sugar()).isEqualByComparingTo("1.060");
    assertThat(nearest.acidity()).isEqualByComparingTo("7.5");
  }

  @Test
  void returnsSamePointWhenAlreadyInside() {
    final var point = new SugarAcidityCoordinates(new BigDecimal("1.070"), new BigDecimal("6.0"));

    assertThat(geometry.nearestPointTo(point)).isEqualTo(point);
  }

  @Test
  void rejectsInvalidRanges() {
    assertThatThrownBy(() -> new RectangleSugarAcidityTargetGeometry(
        new BigDecimal("1.080"),
        new BigDecimal("1.060"),
        new BigDecimal("4.5"),
        new BigDecimal("7.5")
    )).isInstanceOf(IllegalArgumentException.class);
  }
}
