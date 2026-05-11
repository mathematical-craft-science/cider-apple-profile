package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.axis;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.jspecify.annotations.NonNull;


/**
 * Built-in canonical axis boundaries used by cider profile evaluation, graph metadata, and blend scoring.
 */
public final class BuiltInCiderProfileAxisBoundaries {

  public static final BigDecimal SUGAR_MIN = new BigDecimal("1.020");
  public static final BigDecimal SUGAR_LOW_MEDIUM_BOUNDARY = new BigDecimal("1.045");
  public static final BigDecimal SUGAR_MEDIUM_HIGH_BOUNDARY = new BigDecimal("1.060");
  public static final BigDecimal SUGAR_MAX = new BigDecimal("1.080");

  public static final BigDecimal ACIDITY_MIN = new BigDecimal("0");
  public static final BigDecimal ACIDITY_LOW_MEDIUM_BOUNDARY = new BigDecimal("4.5");
  public static final BigDecimal ACIDITY_MEDIUM_HIGH_BOUNDARY = new BigDecimal("7.5");
  public static final BigDecimal ACIDITY_MAX = new BigDecimal("10");

  public static final BigDecimal TANNIN_MIN = TanninityValue.minValue();
  public static final BigDecimal TANNIN_MAX = TanninityValue.maxValue();

  private BuiltInCiderProfileAxisBoundaries() {
  }

  public static @NonNull List<@NonNull BigDecimal> sugarThresholdBreakpoints() {
    return List.of(SUGAR_MIN, SUGAR_LOW_MEDIUM_BOUNDARY, SUGAR_MEDIUM_HIGH_BOUNDARY, SUGAR_MAX);
  }

  public static @NonNull List<@NonNull BigDecimal> acidityThresholdBreakpoints() {
    return List.of(ACIDITY_MIN, ACIDITY_LOW_MEDIUM_BOUNDARY, ACIDITY_MEDIUM_HIGH_BOUNDARY, ACIDITY_MAX);
  }
}
