package org.craft.mcs.cap.shared;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.model.Brix;
import org.craft.mcs.cap.shared.model.Plato;
import org.craft.mcs.cap.shared.model.SpecificGravity;
import org.craft.mcs.cap.shared.model.Tanninity;
import org.craft.mcs.cap.shared.model.TitratableAcidity;
import systems.uom.quantity.Acidity;
import systems.uom.ucum.UCUM;
import tech.units.indriya.AbstractSystemOfUnits;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.unit.AlternateUnit;
import tech.units.indriya.unit.Units;


public final class BeverageUnits
    extends AbstractSystemOfUnits {

  public static final Unit<Brix> BRIX =
      new AlternateUnit<>(AbstractUnit.ONE, "°Bx");

  public static final Unit<Plato> PLATO =
      new AlternateUnit<>(AbstractUnit.ONE, "°P");

  public static final Unit<SpecificGravity> SG =
      new AlternateUnit<>(AbstractUnit.ONE, "SG");

  public static final Unit<Acidity> PH =
      UCUM.PH;

  public static final Unit<TitratableAcidity> GRAMS_PER_LITER_AS_LACTIC_ACID =
      MetricPrefix.MILLI(Units.KILOGRAM)
          .divide(Units.LITRE)
          .asType(TitratableAcidity.class);

  public static final Unit<Tanninity> MILLIGRAM_PER_LITER_AS_TANNIC_ACID =
      MetricPrefix.MILLI(Units.GRAM)
          .divide(Units.LITRE)
          .asType(Tanninity.class);

  private static final BeverageUnits INSTANCE = new BeverageUnits();

  private BeverageUnits() {
    addUnit(BRIX);
    addUnit(PLATO);
    addUnit(SG);
    addUnit(PH);
    addUnit(GRAMS_PER_LITER_AS_LACTIC_ACID);
    addUnit(MILLIGRAM_PER_LITER_AS_TANNIC_ACID);

    SimpleUnitFormat.getInstance().label(BRIX, "°Bx");
    SimpleUnitFormat.getInstance().label(PLATO, "°P");
    SimpleUnitFormat.getInstance().label(SG, "SG");
    SimpleUnitFormat.getInstance().label(PH, "pH");
    SimpleUnitFormat.getInstance().label(GRAMS_PER_LITER_AS_LACTIC_ACID, "g/L as lactic acid");
    SimpleUnitFormat.getInstance().label(MILLIGRAM_PER_LITER_AS_TANNIC_ACID, "mg/L TAE");
  }

  public static BeverageUnits getInstance() {
    return INSTANCE;
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  private <U extends Unit<?>> U addUnit(U unit) {
    this.units.add(unit);
    return unit;
  }
}