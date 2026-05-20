package org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import java.math.MathContext;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record EllipseSugarAcidityZoneGeometry(
    @NonNull SugarAcidityZone zone,
    @NonNull BigDecimal sugarCenter,
    @NonNull BigDecimal acidityCenter,
    @NonNull BigDecimal sugarRadius,
    @NonNull BigDecimal acidityRadius
) implements SugarAcidityZoneGeometry {

  private static final MathContext MC = MathContext.DECIMAL64;

  public EllipseSugarAcidityZoneGeometry {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(sugarCenter, "ellipse sugar center cannot be null");
    requireNonNull(acidityCenter, "ellipse acidity center cannot be null");
    requireNonNull(sugarRadius, "ellipse sugar radius cannot be null");
    requireNonNull(acidityRadius, "ellipse acidity radius cannot be null");
    if (sugarRadius.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainRuleViolationException("ellipse sugar radius must be positive");
    }
    if (acidityRadius.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainRuleViolationException("ellipse acidity radius must be positive");
    }
  }

  @Override
  public @NonNull SugarAcidityZoneGeometryShape shape() {
    return SugarAcidityZoneGeometryShape.ELLIPSE;
  }

  @Override
  public boolean contains(
      @NonNull BigDecimal sugar,
      @NonNull BigDecimal acidity
  ) {
    requireNonNull(sugar, "sugar value cannot be null");
    requireNonNull(acidity, "acidity value cannot be null");
    final var normalizedSugar = sugar.subtract(sugarCenter, MC)
        .divide(sugarRadius, MC);
    final var normalizedAcidity = acidity.subtract(acidityCenter, MC)
        .divide(acidityRadius, MC);
    final var distance = normalizedSugar.multiply(normalizedSugar, MC)
        .add(normalizedAcidity.multiply(normalizedAcidity, MC), MC);

    return distance.compareTo(BigDecimal.ONE) <= 0;
  }
}
