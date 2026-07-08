package org.craft.mcs.cap.core.profileanalysis.classification.profile;

import org.craft.mcs.cap.core.profileanalysis.classification.profile.french.FrenchThresholds;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.longashton.LongAshton4Thresholds;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.longashton.LongAshton7Thresholds;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.northamerican.NorthAmericanThresholds;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.spanish.SpanishThresholds;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.jspecify.annotations.NonNull;


public class BuiltInCiderClassificationThresholds {

  private BuiltInCiderClassificationThresholds() {
  }

  // =========================
  // NORTH AMERICAN
  // =========================

  public static @NonNull NorthAmericanThresholds northAmerican() {
    return new NorthAmericanThresholds(

        // Sugar (Specific Gravity)
        AxisRange.lessThan("1.045"),
        AxisRange.between("1.045", "1.060"),
        AxisRange.between("1.060", "1.070"),
        AxisRange.atLeast("1.070"),

        // Acidity (g/L malic)
        AxisRange.lessThan("4.5"),
        AxisRange.between("4.5", "7.5"),
        AxisRange.between("7.5", "11.0"),
        AxisRange.atLeast("11.0"),

        // Tannin (mg/L TAE)
        AxisRange.lessThan("1500"),
        AxisRange.between("1500", "2500"),
        AxisRange.atLeast("2500")
    );
  }

  // =========================
  // LONG ASHTON 4
  // =========================

  public static @NonNull LongAshton4Thresholds longAshton4() {
    return new LongAshton4Thresholds(
        AxisRange.lessThan("4.5"), // 4.5 g/L malic acid
        AxisRange.atLeast("4.5"), // 4.5 g/L malic acid
        AxisRange.lessThan("2000"), // 2000 mg/L TAE
        AxisRange.atLeast("2000") // 2000 mg/L TAE
    );
  }

  // =========================
  // LONG ASHTON 7
  // =========================

  public static @NonNull LongAshton7Thresholds longAshton7() {
    return new LongAshton7Thresholds(
        AxisRange.lessThan("4.5"), // 4.5 g/L malic acid
        AxisRange.atLeast("4.5"), // 4.5 g/L malic acid

        AxisRange.lessThan("1000"), // 1000 mg/L TAE
        AxisRange.between("1000", "1500"), // 1000-1500 mg/L TAE
        AxisRange.between("1500", "2000"), // 1500-2000 mg/L TAE
        AxisRange.atLeast("2000"), // 2000 mg/L TAE

        AxisRange.lessThan("2000"), // 2000 mg/L TAE
        AxisRange.between("2000", "3000"), // 2000-3000 mg/L TAE
        AxisRange.atLeast("3000") // 3000 mg/L TAE
    );
  }

  // =========================
  // FRENCH (Jolicoeur Chapter 5)
  // =========================

  public static @NonNull FrenchThresholds french() {
    return new FrenchThresholds(
        AxisRange.lessThan("4.0"), // 4.0 g/L malic acid
        AxisRange.between("4.0", "6.0"), // 4.0-6.0 g/L malic acid
        AxisRange.atLeast("6.0"), // 6.0 g/L malic acid

        AxisRange.lessThan("2000"), // 2000 mg/L TAE
        AxisRange.between("2000", "3000"), // 2000-3000 mg/L TAE
        AxisRange.atLeast("3000") // 3000 mg/L TAE
    );
  }

  // =========================
  // SPANISH (Jolicoeur Chapter 5)
  // =========================

  public static @NonNull SpanishThresholds spanish() {
    return new SpanishThresholds(
        AxisRange.lessThan("4.9"), // 4.9 g/L malic acid
        AxisRange.between("4.9", "6.6"), // 4.9-6.6 g/L malic acid
        AxisRange.atLeast("6.6"), // 6.6.5 g/L malic acid

        AxisRange.lessThan("1450"), // 1450 mg/L TAE
        AxisRange.between("1450", "2000"), // 1450-2000 mg/L TAE
        AxisRange.atLeast("2000") // 2000 mg/L TAE
    );
  }
}
