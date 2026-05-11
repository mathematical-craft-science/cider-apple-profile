package org.craft.mcs.cap.shared.kernel.measurement.unit;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import javax.measure.quantity.Volume;
import org.jspecify.annotations.NonNull;
import systems.uom.quantity.Acidity;
import systems.uom.ucum.UCUM;
import tech.units.indriya.AbstractSystemOfUnits;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.AlternateUnit;
import tech.units.indriya.unit.TransformedUnit;
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

  public static final Unit<TitratableAcidity> GRAM_PER_LITER_AS_MALIC_ACID =
      MetricPrefix.MILLI(Units.KILOGRAM)
          .divide(Units.LITRE)
          .asType(TitratableAcidity.class);


  public static final Unit<TitratableAcidity> GRAM_PER_LITRE_AS_LACTIC_ACID =
      new TransformedUnit<>(
          "g/L as lactic acid",
          GRAM_PER_LITER_AS_MALIC_ACID,
          MultiplyConverter.ofRational(670435, 900780)
      );

  public static final Unit<TitratableAcidity> GRAM_PER_LITRE_AS_CITRIC_ACID =
      new TransformedUnit<>(
          "g/L as citric acid",
          GRAM_PER_LITER_AS_MALIC_ACID,
          MultiplyConverter.ofRational(670435, 640413)
      );

  public static final Unit<Tanninity> MILLIGRAM_PER_LITER_AS_TANNIC_ACID =
      MetricPrefix.MILLI(Units.GRAM)
          .divide(Units.LITRE)
          .asType(Tanninity.class);

  public static final Unit<AlcoholByVolume> PERCENT_ABV =
      Units.PERCENT.asType(AlcoholByVolume.class);

  public static final Unit<Volume> LITRE =
      Units.LITRE.asType(Volume.class);

  public static final Unit<TitratableAcidity> TA = GRAM_PER_LITER_AS_MALIC_ACID;

  private static final BeverageUnits INSTANCE = new BeverageUnits();

  private BeverageUnits() {
    addUnit(BRIX);
    addUnit(PLATO);
    addUnit(SG);
    addUnit(PH);
    addUnit(PERCENT_ABV);
    addUnit(LITRE);
    //
    addUnit(GRAM_PER_LITER_AS_MALIC_ACID);
    addUnit(GRAM_PER_LITRE_AS_LACTIC_ACID);
    addUnit(GRAM_PER_LITRE_AS_CITRIC_ACID);
    //
    addUnit(MILLIGRAM_PER_LITER_AS_TANNIC_ACID);

    final var f = SimpleUnitFormat.getInstance();

    f.label(BRIX, "°Bx");
    f.label(PLATO, "°P");
    f.label(SG, "SG");
    f.label(PH, "pH");
    f.label(PERCENT_ABV, "% ABV");
    f.label(LITRE, "L");
    //
    f.label(GRAM_PER_LITER_AS_MALIC_ACID, "g/L as malic acid");
    f.label(GRAM_PER_LITRE_AS_LACTIC_ACID, "g/L as lactic acid");
    f.label(GRAM_PER_LITRE_AS_CITRIC_ACID, "g/L as citric acid");
    //
    f.label(MILLIGRAM_PER_LITER_AS_TANNIC_ACID, "mg/L TAE");

  }

  public static BeverageUnits getInstance() {
    return INSTANCE;
  }

  @NonNull
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  private <U extends Unit<?>> U addUnit(U unit) {
    this.units.add(unit);
    return unit;
  }
}
