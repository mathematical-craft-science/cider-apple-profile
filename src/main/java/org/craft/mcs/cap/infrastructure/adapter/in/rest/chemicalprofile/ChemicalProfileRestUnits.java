package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile;

import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jspecify.annotations.NonNull;


/**
 * Canonical unit labels exposed by the REST chemical-profile transport.
 *
 * <p>The service core stores values in canonical units; this helper keeps transport labels aligned
 * with the JSR-385 units registered in {@link BeverageUnits} instead of duplicating string
 * literals across mappers and ACL normalizers.
 */
public final class ChemicalProfileRestUnits {

  public static final @NonNull String SUGAR = BeverageUnits.label(BeverageUnits.SG);
  public static final @NonNull String PH = BeverageUnits.label(BeverageUnits.PH);
  public static final @NonNull String TITRATABLE_ACIDITY =
      BeverageUnits.label(BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID);
  public static final @NonNull String TANNINITY =
      BeverageUnits.label(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID);
  public static final @NonNull String YEAST_ASSIMILABLE_NITROGEN =
      BeverageUnits.label(BeverageUnits.YAN);

  private ChemicalProfileRestUnits() {
  }
}
