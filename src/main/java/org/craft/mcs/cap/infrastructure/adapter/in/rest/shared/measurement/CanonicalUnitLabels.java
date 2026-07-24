package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Canonical unit labels exposed by REST transport DTOs.
 *
 * <p>Core values are normalized to CAP canonical units. REST responses should return physical
 * numeric values together with these labels so API clients do not duplicate domain unit knowledge.
 */
public final class CanonicalUnitLabels {

  public static final @NonNull String SUGAR = BeverageUnits.label(BeverageUnits.SG);
  public static final @NonNull String SUGAR_SHORT = "SG";
  public static final @NonNull String PH = BeverageUnits.label(BeverageUnits.PH);
  public static final @NonNull String PH_SHORT = "pH";
  public static final @NonNull String TITRATABLE_ACIDITY =
      BeverageUnits.label(BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID);
  public static final @NonNull String TITRATABLE_ACIDITY_SHORT = "g/L";
  public static final @NonNull String TANNIN =
      BeverageUnits.label(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID);
  public static final @NonNull String TANNIN_SHORT = "mg/L";
  public static final @NonNull String YEAST_ASSIMILABLE_NITROGEN =
      BeverageUnits.label(BeverageUnits.YAN);
  public static final @NonNull String YEAST_ASSIMILABLE_NITROGEN_SHORT = "mg N/L";
  public static final @NonNull String ABV = BeverageUnits.label(BeverageUnits.PERCENT_ABV);
  public static final @NonNull String ABV_SHORT = "% ABV";
  public static final @NonNull String VOLUME = BeverageUnits.label(BeverageUnits.LITRE);
  public static final @NonNull String VOLUME_SHORT = "L";
  public static final @NonNull String TEMPERATURE = BeverageUnits.label(BeverageUnits.CELSIUS);
  public static final @NonNull String TEMPERATURE_SHORT = TEMPERATURE;
  public static final @NonNull String TURBIDITY = BeverageUnits.label(BeverageUnits.NTU);
  public static final @NonNull String TURBIDITY_SHORT = TURBIDITY;

  private CanonicalUnitLabels() {
  }

  public static @NonNull String forAxis(@NonNull ChemicalAxis axis) {
    requireNonNull(axis, "chemical axis cannot be null");

    return switch (axis) {
      case ChemicalAxis.SUGAR -> SUGAR;
      case ChemicalAxis.ACIDITY -> TITRATABLE_ACIDITY;
      case ChemicalAxis.TANNIN -> TANNIN;
      case ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN -> YEAST_ASSIMILABLE_NITROGEN;
    };
  }

  public static @NonNull String forAxis(@NonNull ProfileEvaluationAxis axis) {
    requireNonNull(axis, "profile evaluation axis cannot be null");

    return switch (axis) {
      case ProfileEvaluationAxis.SUGAR -> SUGAR;
      case ProfileEvaluationAxis.ACIDITY -> TITRATABLE_ACIDITY;
      case ProfileEvaluationAxis.TANNIN -> TANNIN;
    };
  }

  public static @NonNull String forCharacteristic(
      @NonNull MeasurementCharacteristic characteristic
  ) {
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    return switch (characteristic) {
      case SPECIFIC_GRAVITY -> SUGAR;
      case PH -> PH;
      case TITRATABLE_ACIDITY -> TITRATABLE_ACIDITY;
      case TANNIN -> TANNIN;
      case YAN -> YEAST_ASSIMILABLE_NITROGEN;
      case TEMPERATURE -> TEMPERATURE;
      case TURBIDITY -> TURBIDITY;
    };
  }
}
