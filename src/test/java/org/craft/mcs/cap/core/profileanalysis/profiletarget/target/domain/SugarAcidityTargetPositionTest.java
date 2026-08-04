package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;


class SugarAcidityTargetPositionTest {

  @Test
  void isInsideOnlyWhenBothCoordinatesBelongToTarget() {
    final var inside = new SugarAcidityTargetPosition(
        MeasuredValueRangePosition.WITHIN,
        MeasuredValueRangePosition.WITHIN
    );
    final var excludedUpperBoundary = new SugarAcidityTargetPosition(
        MeasuredValueRangePosition.ABOVE,
        MeasuredValueRangePosition.WITHIN
    );

    assertThat(inside.inside()).isTrue();
    assertThat(excludedUpperBoundary.inside()).isFalse();
  }

  @Test
  void rejectsMissingCoordinatePosition() {
    assertThatNullPointerException()
        .isThrownBy(() -> new SugarAcidityTargetPosition(
            null,
            MeasuredValueRangePosition.WITHIN
        ))
        .withMessage("specific gravity target position cannot be null");
  }
}
