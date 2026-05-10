package org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.rule.AxisRange;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.plot.EllipseSugarAcidityGraphZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.plot.RectangleSugarAcidityGraphZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.plot.SugarAcidityGraphAxis;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.plot.SugarAcidityGraphMetadata;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.plot.SugarAcidityGraphRange;
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

  public static @NonNull SugarAcidityGraphMetadata rectangularGraphMetadata() {
    final var sugarMin = new BigDecimal("1.020");
    final var sugarLowMediumBoundary = new BigDecimal("1.045");
    final var sugarMediumHighBoundary = new BigDecimal("1.060");
    final var sugarMax = new BigDecimal("1.080");

    final var acidityMin = new BigDecimal("0");
    final var acidityLowMediumBoundary = new BigDecimal("4.5");
    final var acidityMediumHighBoundary = new BigDecimal("7.5");
    final var acidityMax = new BigDecimal("10");

    return new SugarAcidityGraphMetadata(
        new SugarAcidityGraphAxis(
            "SG",
            "Specific gravity",
            "SG",
            sugarMin,
            sugarMax,
            List.of(sugarLowMediumBoundary, sugarMediumHighBoundary)
        ),
        new SugarAcidityGraphAxis(
            "TA",
            "Total acidity",
            "g/L malic",
            acidityMin,
            acidityMax,
            List.of(acidityLowMediumBoundary, acidityMediumHighBoundary)
        ),
        List.of(
            new RectangleSugarAcidityGraphZone(
                SugarAcidityZone.PERFECT_APPLES,
                new SugarAcidityGraphRange(sugarMediumHighBoundary, sugarMax),
                new SugarAcidityGraphRange(acidityLowMediumBoundary, acidityMediumHighBoundary)
            ),
            new RectangleSugarAcidityGraphZone(
                SugarAcidityZone.SUGAR_RICH_APPLES,
                new SugarAcidityGraphRange(sugarMediumHighBoundary, sugarMax),
                new SugarAcidityGraphRange(acidityMediumHighBoundary, acidityMax)
            ),
            new RectangleSugarAcidityGraphZone(
                SugarAcidityZone.LOW_ACIDITY_APPLES,
                new SugarAcidityGraphRange(sugarMin, sugarMax),
                new SugarAcidityGraphRange(acidityMin, acidityLowMediumBoundary)
            ),
            new RectangleSugarAcidityGraphZone(
                SugarAcidityZone.MEDIUM_SUGAR_APPLES,
                new SugarAcidityGraphRange(sugarLowMediumBoundary, sugarMediumHighBoundary),
                new SugarAcidityGraphRange(acidityLowMediumBoundary, acidityMax)
            ),
            new RectangleSugarAcidityGraphZone(
                SugarAcidityZone.LOW_SUGAR_APPLES,
                new SugarAcidityGraphRange(sugarMin, sugarLowMediumBoundary),
                new SugarAcidityGraphRange(acidityLowMediumBoundary, acidityMax)
            )
        )
    );
  }

  public static @NonNull SugarAcidityGraphMetadata ellipticalGraphMetadata() {
    return new SugarAcidityGraphMetadata(
        new SugarAcidityGraphAxis(
            "SG",
            "Specific gravity",
            "SG",
            new BigDecimal("1.020"),
            new BigDecimal("1.080"),
            List.of(new BigDecimal("1.045"), new BigDecimal("1.060"))
        ),
        new SugarAcidityGraphAxis(
            "TA",
            "Total acidity",
            "g/L malic",
            new BigDecimal("0"),
            new BigDecimal("10"),
            List.of(new BigDecimal("4.5"), new BigDecimal("7.5"))
        ),
        List.of(
            new EllipseSugarAcidityGraphZone(
                SugarAcidityZone.PERFECT_APPLES,
                new BigDecimal("1.065"),
                new BigDecimal("5.9"),
                new BigDecimal("0.008"),
                new BigDecimal("1.2")
            ),
            new EllipseSugarAcidityGraphZone(
                SugarAcidityZone.SUGAR_RICH_APPLES,
                new BigDecimal("1.066"),
                new BigDecimal("8.7"),
                new BigDecimal("0.007"),
                new BigDecimal("1.8")
            ),
            new EllipseSugarAcidityGraphZone(
                SugarAcidityZone.LOW_ACIDITY_APPLES,
                new BigDecimal("1.051"),
                new BigDecimal("2.9"),
                new BigDecimal("0.014"),
                new BigDecimal("1.9")
            ),
            new EllipseSugarAcidityGraphZone(
                SugarAcidityZone.MEDIUM_SUGAR_APPLES,
                new BigDecimal("1.052"),
                new BigDecimal("7.7"),
                new BigDecimal("0.007"),
                new BigDecimal("2.8")
            ),
            new EllipseSugarAcidityGraphZone(
                SugarAcidityZone.LOW_SUGAR_APPLES,
                new BigDecimal("1.040"),
                new BigDecimal("7.4"),
                new BigDecimal("0.006"),
                new BigDecimal("3.0")
            )
        )
    );
  }
}
