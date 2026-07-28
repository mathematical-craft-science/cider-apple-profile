package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphAxisScale;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;


/** Provides geometry variants of the built-in Jolicoeur sugar-acidity graph system. */
public final class BuiltInJolicoeurSugarAcidityZoneProfiles {

  private static final SugarAcidityGraphAxisScale<SpecificGravityValue> SPECIFIC_GRAVITY_SCALE =
      new SugarAcidityGraphAxisScale<>(
          SpecificGravityValue.of("1.020"),
          SpecificGravityValue.of("1.080"),
          List.of(
              SpecificGravityValue.of("1.045"),
              SpecificGravityValue.of("1.060")
          )
      );
  private static final SugarAcidityGraphAxisScale<TitratableAcidityValue> TITRATABLE_ACIDITY_SCALE =
      new SugarAcidityGraphAxisScale<>(
          TitratableAcidityValue.of(BigDecimal.ZERO),
          TitratableAcidityValue.of("10"),
          List.of(
              TitratableAcidityValue.of("4.5"),
              TitratableAcidityValue.of("7.5")
          )
      );

  private BuiltInJolicoeurSugarAcidityZoneProfiles() {
  }

  public static @NonNull JolicoeurSugarAcidityZoneGeometryProfile rectangularZoneGeometryProfile() {
    final var sugarLowMedium = SPECIFIC_GRAVITY_SCALE.boundaryValues().getFirst();
    final var sugarMediumHigh = SPECIFIC_GRAVITY_SCALE.boundaryValues().getLast();
    final var acidityLowMedium = TITRATABLE_ACIDITY_SCALE.boundaryValues().getFirst();
    final var acidityMediumHigh = TITRATABLE_ACIDITY_SCALE.boundaryValues().getLast();
    return new JolicoeurSugarAcidityZoneGeometryProfile(
        List.of(
            new RectangleSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.PERFECT_APPLES,
                MeasuredValueRange.closedOpen(
                    sugarMediumHigh,
                    SPECIFIC_GRAVITY_SCALE.max()
                ),
                MeasuredValueRange.closedOpen(
                    acidityLowMedium,
                    acidityMediumHigh
                )
            ),
            new RectangleSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.HIGH_SUGAR_APPLES,
                MeasuredValueRange.closedOpen(
                    sugarMediumHigh,
                    SPECIFIC_GRAVITY_SCALE.max()
                ),
                MeasuredValueRange.closedOpen(
                    acidityMediumHigh,
                    TITRATABLE_ACIDITY_SCALE.max()
                )
            ),
            new RectangleSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.LOW_ACIDITY_APPLES,
                MeasuredValueRange.closedOpen(
                    SPECIFIC_GRAVITY_SCALE.min(),
                    SPECIFIC_GRAVITY_SCALE.max()
                ),
                MeasuredValueRange.closedOpen(
                    TITRATABLE_ACIDITY_SCALE.min(),
                    acidityLowMedium
                )
            ),
            new RectangleSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.MEDIUM_SUGAR_APPLES,
                MeasuredValueRange.closedOpen(
                    sugarLowMedium,
                    sugarMediumHigh
                ),
                MeasuredValueRange.closedOpen(
                    acidityLowMedium,
                    TITRATABLE_ACIDITY_SCALE.max()
                )
            ),
            new RectangleSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.LOW_SUGAR_APPLES,
                MeasuredValueRange.closedOpen(
                    SPECIFIC_GRAVITY_SCALE.min(),
                    sugarLowMedium
                ),
                MeasuredValueRange.closedOpen(
                    acidityLowMedium,
                    TITRATABLE_ACIDITY_SCALE.max()
                )
            )
        ),
        SPECIFIC_GRAVITY_SCALE,
        TITRATABLE_ACIDITY_SCALE
    );
  }

  public static @NonNull JolicoeurSugarAcidityZoneGeometryProfile ellipticalZoneGeometryProfile() {
    return new JolicoeurSugarAcidityZoneGeometryProfile(
        List.of(
            new EllipseSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.PERFECT_APPLES,
                SpecificGravityValue.of("1.065"),
                TitratableAcidityValue.of("5.9"),
                new BigDecimal("0.008"),
                new BigDecimal("1.2")
            ),
            new EllipseSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.HIGH_SUGAR_APPLES,
                SpecificGravityValue.of("1.066"),
                TitratableAcidityValue.of("8.7"),
                new BigDecimal("0.007"),
                new BigDecimal("1.8")
            ),
            new EllipseSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.LOW_ACIDITY_APPLES,
                SpecificGravityValue.of("1.051"),
                TitratableAcidityValue.of("2.9"),
                new BigDecimal("0.014"),
                new BigDecimal("1.9")
            ),
            new EllipseSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.MEDIUM_SUGAR_APPLES,
                SpecificGravityValue.of("1.052"),
                TitratableAcidityValue.of("7.7"),
                new BigDecimal("0.007"),
                new BigDecimal("2.8")
            ),
            new EllipseSugarAcidityZoneGeometry(
                JolicoeurSugarAcidityZone.LOW_SUGAR_APPLES,
                SpecificGravityValue.of("1.040"),
                TitratableAcidityValue.of("7.4"),
                new BigDecimal("0.006"),
                new BigDecimal("3.0")
            )
        ),
        SPECIFIC_GRAVITY_SCALE,
        TITRATABLE_ACIDITY_SCALE
    );
  }
}
