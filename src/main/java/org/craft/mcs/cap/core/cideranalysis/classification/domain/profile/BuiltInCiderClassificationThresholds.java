package org.craft.mcs.cap.core.cideranalysis.classification.domain.profile;

import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.longashton.LongAshton4Thresholds;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.longashton.LongAshton7Thresholds;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.northamerican.NorthAmericanThresholds;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;


/**
 * Built-in thresholds owned by explicitly named cider-apple classification systems.
 *
 * <p>Values from one returned profile must not be treated as canonical characteristic levels or
 * reused as graph geometry and target-scoring calibration.</p>
 */
public final class BuiltInCiderClassificationThresholds {

  private BuiltInCiderClassificationThresholds() {
  }

  // =========================
  // NORTH AMERICAN
  // =========================

  public static @NonNull NorthAmericanThresholds northAmerican() {
    return new NorthAmericanThresholds(

        // Sugar (Specific Gravity)
        ClassificationValueRange.lessThan("1.045"),
        ClassificationValueRange.between("1.045", "1.060"),
        ClassificationValueRange.between("1.060", "1.070"),
        ClassificationValueRange.atLeast("1.070"),

        // Acidity (g/L malic)
        ClassificationValueRange.lessThan("4.5"),
        ClassificationValueRange.between("4.5", "7.5"),
        ClassificationValueRange.between("7.5", "11.0"),
        ClassificationValueRange.atLeast("11.0"),

        // Tannin (mg/L TAE)
        ClassificationValueRange.lessThan("1500"),
        ClassificationValueRange.between("1500", "2500"),
        ClassificationValueRange.atLeast("2500")
    );
  }

  // =========================
  // LONG ASHTON 4
  // =========================

  public static @NonNull LongAshton4Thresholds longAshton4() {
    return new LongAshton4Thresholds(
        ClassificationValueRange.lessThan("4.5"), // 4.5 g/L malic acid
        ClassificationValueRange.atLeast("4.5"), // 4.5 g/L malic acid
        ClassificationValueRange.lessThan("2000"), // 2000 mg/L TAE
        ClassificationValueRange.atLeast("2000") // 2000 mg/L TAE
    );
  }

  // =========================
  // LONG ASHTON 7
  // =========================

  public static @NonNull LongAshton7Thresholds longAshton7() {
    return new LongAshton7Thresholds(
        ClassificationValueRange.lessThan("4.5"), // 4.5 g/L malic acid
        ClassificationValueRange.atLeast("4.5"), // 4.5 g/L malic acid

        ClassificationValueRange.lessThan("1000"), // 1000 mg/L TAE
        ClassificationValueRange.between("1000", "1500"), // 1000-1500 mg/L TAE
        ClassificationValueRange.between("1500", "2000"), // 1500-2000 mg/L TAE
        ClassificationValueRange.atLeast("2000"), // 2000 mg/L TAE

        ClassificationValueRange.lessThan("2000"), // 2000 mg/L TAE
        ClassificationValueRange.between("2000", "3000"), // 2000-3000 mg/L TAE
        ClassificationValueRange.atLeast("3000") // 3000 mg/L TAE
    );
  }

  // =========================
  // FRENCH (Jolicoeur Chapter 5)
  // =========================

  public static @NonNull AcidityTanninThresholds french() {
    return new AcidityTanninThresholds(
        ClassificationValueRange.lessThan("4.0"), // 4.0 g/L malic acid
        ClassificationValueRange.between("4.0", "6.0"), // 4.0-6.0 g/L malic acid
        ClassificationValueRange.atLeast("6.0"), // 6.0 g/L malic acid

        ClassificationValueRange.lessThan("2000"), // 2000 mg/L TAE
        ClassificationValueRange.between("2000", "3000"), // 2000-3000 mg/L TAE
        ClassificationValueRange.atLeast("3000") // 3000 mg/L TAE
    );
  }

  // =========================
  // SPANISH (Jolicoeur Chapter 5)
  // =========================

  public static @NonNull AcidityTanninThresholds spanish() {
    return new AcidityTanninThresholds(
        ClassificationValueRange.lessThan("4.9"), // 4.9 g/L malic acid
        ClassificationValueRange.between("4.9", "6.6"), // 4.9-6.6 g/L malic acid
        ClassificationValueRange.atLeast("6.6"), // 6.6 g/L malic acid

        ClassificationValueRange.lessThan("1450"), // 1450 mg/L TAE
        ClassificationValueRange.between("1450", "2000"), // 1450-2000 mg/L TAE
        ClassificationValueRange.atLeast("2000") // 2000 mg/L TAE
    );
  }
}
