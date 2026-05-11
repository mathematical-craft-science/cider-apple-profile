package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.axis.BuiltInCiderProfileAxisBoundaries;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.jspecify.annotations.NonNull;


public final class BuiltInSugarAcidityZoneProfiles {

  private BuiltInSugarAcidityZoneProfiles() {
  }

  public static @NonNull SugarAcidityZoneThresholds thresholds() {
    return new SugarAcidityZoneThresholds(

        // Sugar (Specific Gravity)
        AxisRange.lessThan("1.045"),
        AxisRange.between("1.045", "1.060"),
        AxisRange.atLeast("1.060"),

        // Acidity (g/L malic)
        AxisRange.lessThan("4.5"),
        AxisRange.between("4.5", "7.5"),
        AxisRange.atLeast("7.5"),

        // Tannin (mg/L TAE)
        AxisRange.between("1500", "2500")
    );
  }

  public static @NonNull SugarAcidityZoneGeometryProfile rectangularZoneGeometryProfile() {
    return new SugarAcidityZoneGeometryProfile(List.of(
        new RectangleSugarAcidityZoneGeometry(
            SugarAcidityZone.PERFECT_APPLES,
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.SUGAR_MEDIUM_HIGH_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.SUGAR_MAX
            ),
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.ACIDITY_LOW_MEDIUM_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.ACIDITY_MEDIUM_HIGH_BOUNDARY
            )
        ),
        new RectangleSugarAcidityZoneGeometry(
            SugarAcidityZone.SUGAR_RICH_APPLES,
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.SUGAR_MEDIUM_HIGH_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.SUGAR_MAX
            ),
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.ACIDITY_MEDIUM_HIGH_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.ACIDITY_MAX
            )
        ),
        new RectangleSugarAcidityZoneGeometry(
            SugarAcidityZone.LOW_ACIDITY_APPLES,
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.SUGAR_MIN,
                BuiltInCiderProfileAxisBoundaries.SUGAR_MAX
            ),
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.ACIDITY_MIN,
                BuiltInCiderProfileAxisBoundaries.ACIDITY_LOW_MEDIUM_BOUNDARY
            )
        ),
        new RectangleSugarAcidityZoneGeometry(
            SugarAcidityZone.MEDIUM_SUGAR_APPLES,
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.SUGAR_LOW_MEDIUM_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.SUGAR_MEDIUM_HIGH_BOUNDARY
            ),
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.ACIDITY_LOW_MEDIUM_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.ACIDITY_MAX
            )
        ),
        new RectangleSugarAcidityZoneGeometry(
            SugarAcidityZone.LOW_SUGAR_APPLES,
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.SUGAR_MIN,
                BuiltInCiderProfileAxisBoundaries.SUGAR_LOW_MEDIUM_BOUNDARY
            ),
            new SugarAcidityZoneRange(
                BuiltInCiderProfileAxisBoundaries.ACIDITY_LOW_MEDIUM_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.ACIDITY_MAX
            )
        )
    ));
  }

  public static @NonNull SugarAcidityZoneGeometryProfile ellipticalZoneGeometryProfile() {
    return new SugarAcidityZoneGeometryProfile(List.of(
        new EllipseSugarAcidityZoneGeometry(
            SugarAcidityZone.PERFECT_APPLES,
            new BigDecimal("1.065"),
            new BigDecimal("5.9"),
            new BigDecimal("0.008"),
            new BigDecimal("1.2")
        ),
        new EllipseSugarAcidityZoneGeometry(
            SugarAcidityZone.SUGAR_RICH_APPLES,
            new BigDecimal("1.066"),
            new BigDecimal("8.7"),
            new BigDecimal("0.007"),
            new BigDecimal("1.8")
        ),
        new EllipseSugarAcidityZoneGeometry(
            SugarAcidityZone.LOW_ACIDITY_APPLES,
            new BigDecimal("1.051"),
            new BigDecimal("2.9"),
            new BigDecimal("0.014"),
            new BigDecimal("1.9")
        ),
        new EllipseSugarAcidityZoneGeometry(
            SugarAcidityZone.MEDIUM_SUGAR_APPLES,
            new BigDecimal("1.052"),
            new BigDecimal("7.7"),
            new BigDecimal("0.007"),
            new BigDecimal("2.8")
        ),
        new EllipseSugarAcidityZoneGeometry(
            SugarAcidityZone.LOW_SUGAR_APPLES,
            new BigDecimal("1.040"),
            new BigDecimal("7.4"),
            new BigDecimal("0.006"),
            new BigDecimal("3.0")
        )
    ));
  }
}
