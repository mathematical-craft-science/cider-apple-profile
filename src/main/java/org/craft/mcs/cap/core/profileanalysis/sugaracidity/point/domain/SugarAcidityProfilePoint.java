package org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Axis projection used by sugar-acidity analysis, with optional tannin context.
 */
public record SugarAcidityProfilePoint(
    @NonNull AxisValue sugar,
    @NonNull AxisValue acidity,
    @Nullable AxisValue tannin
) {

  public SugarAcidityProfilePoint {
    requireAxis(sugar, ChemicalAxis.SUGAR, "sugar point");
    requireAxis(acidity, ChemicalAxis.ACIDITY, "acidity point");
    if (tannin != null) {
      requireAxis(tannin, ChemicalAxis.TANNIN, "tannin point");
    }
  }

  private static void requireAxis(
      @NonNull AxisValue value,
      @NonNull ChemicalAxis expectedAxis,
      @NonNull String fieldName
  ) {
    requireNonNull(value, fieldName + " cannot be null");
    if (value.axis() != expectedAxis) {
      throw new DomainRuleViolationException(
          "%s must use %s axis, but was %s".formatted(fieldName, expectedAxis, value.axis())
      );
    }
  }
}
