package org.craft.mcs.cap.shared.domain.classification.profile;

import org.craft.mcs.cap.shared.domain.classification.profile.french.FrenchThresholds;
import org.craft.mcs.cap.shared.domain.classification.profile.longashton.LongAshton4Thresholds;
import org.craft.mcs.cap.shared.domain.classification.profile.longashton.LongAshton7Thresholds;
import org.craft.mcs.cap.shared.domain.classification.profile.spanish.SpanishThresholds;
import org.craft.mcs.cap.shared.domain.classification.rule.AxisRange;
import org.jspecify.annotations.NonNull;


public class BuiltInCiderClassificationThresholds {

  private BuiltInCiderClassificationThresholds() {
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
