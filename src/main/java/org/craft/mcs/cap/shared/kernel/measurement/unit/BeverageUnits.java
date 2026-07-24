package org.craft.mcs.cap.shared.kernel.measurement.unit;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import javax.measure.quantity.Temperature;
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

  public static final Unit<Tannin> MILLIGRAM_PER_LITER_AS_TANNIC_ACID =
      MetricPrefix.MILLI(Units.GRAM)
          .divide(Units.LITRE)
          .asType(Tannin.class);

  public static final Unit<Tannin> GRAM_PER_LITER_AS_TANNIC_ACID =
      Units.GRAM
          .divide(Units.LITRE)
          .asType(Tannin.class);

  public static final Unit<YeastAssimilableNitrogen> MILLIGRAM_NITROGEN_PER_LITER =
      MetricPrefix.MILLI(Units.GRAM)
          .divide(Units.LITRE)
          .asType(YeastAssimilableNitrogen.class);

  public static final Unit<AlcoholByVolume> PERCENT_ABV =
      Units.PERCENT.asType(AlcoholByVolume.class);

  public static final Unit<Volume> LITRE =
      Units.LITRE.asType(Volume.class);

  public static final Unit<Temperature> CELSIUS =
      Units.CELSIUS;

  public static final Unit<Turbidity> NTU =
      new AlternateUnit<>(AbstractUnit.ONE, "NTU");

  public static final Unit<Turbidity> FTU =
      new AlternateUnit<>(AbstractUnit.ONE, "FTU");

  public static final Unit<TitratableAcidity> TA = GRAM_PER_LITER_AS_MALIC_ACID;
  public static final Unit<YeastAssimilableNitrogen> YAN = MILLIGRAM_NITROGEN_PER_LITER;

  private static final BeverageUnits INSTANCE = new BeverageUnits();

  private BeverageUnits() {
    addUnit(BRIX);
    addUnit(PLATO);
    addUnit(SG);
    addUnit(PH);
    addUnit(PERCENT_ABV);
    addUnit(LITRE);
    addUnit(CELSIUS);
    addUnit(NTU);
    addUnit(FTU);
    //
    addUnit(GRAM_PER_LITER_AS_MALIC_ACID);
    addUnit(GRAM_PER_LITRE_AS_LACTIC_ACID);
    addUnit(GRAM_PER_LITRE_AS_CITRIC_ACID);
    //
    addUnit(MILLIGRAM_PER_LITER_AS_TANNIC_ACID);
    addUnit(GRAM_PER_LITER_AS_TANNIC_ACID);
    //
    addUnit(MILLIGRAM_NITROGEN_PER_LITER);

    final var f = SimpleUnitFormat.getInstance();

    f.label(BRIX, "°Bx");
    f.label(PLATO, "°P");
    f.label(SG, "SG");
    f.label(PH, "pH");
    f.label(PERCENT_ABV, "% ABV");
    f.label(LITRE, "L");
    f.label(CELSIUS, "°C");
    f.label(NTU, "NTU");
    f.label(FTU, "FTU");
    //
    f.label(GRAM_PER_LITER_AS_MALIC_ACID, "g/L as malic acid");
    f.label(GRAM_PER_LITRE_AS_LACTIC_ACID, "g/L as lactic acid");
    f.label(GRAM_PER_LITRE_AS_CITRIC_ACID, "g/L as citric acid");
    //
    f.label(MILLIGRAM_PER_LITER_AS_TANNIC_ACID, "mg/L TAE");
    f.label(GRAM_PER_LITER_AS_TANNIC_ACID, "g/L TAE");
    //
    f.label(MILLIGRAM_NITROGEN_PER_LITER, "mg N/L");

  }

  public static BeverageUnits getInstance() {
    return INSTANCE;
  }

  public static @NonNull String label(@NonNull Unit<?> unit) {
    if (unit == BRIX) {
      return "°Bx";
    }
    if (unit == PLATO) {
      return "°P";
    }
    if (unit == SG) {
      return "SG";
    }
    if (unit == PH) {
      return "pH";
    }
    if (unit == PERCENT_ABV) {
      return "% ABV";
    }
    if (unit == LITRE) {
      return "L";
    }
    if (unit == CELSIUS) {
      return "°C";
    }
    if (unit == NTU) {
      return "NTU";
    }
    if (unit == FTU) {
      return "FTU";
    }
    if (unit == GRAM_PER_LITER_AS_MALIC_ACID) {
      return "g/L as malic acid";
    }
    if (unit == GRAM_PER_LITRE_AS_LACTIC_ACID) {
      return "g/L as lactic acid";
    }
    if (unit == GRAM_PER_LITRE_AS_CITRIC_ACID) {
      return "g/L as citric acid";
    }
    if (unit == MILLIGRAM_PER_LITER_AS_TANNIC_ACID) {
      return "mg/L TAE";
    }
    if (unit == GRAM_PER_LITER_AS_TANNIC_ACID) {
      return "g/L TAE";
    }
    if (unit == MILLIGRAM_NITROGEN_PER_LITER) {
      return "mg N/L";
    }
    getInstance();
    return SimpleUnitFormat.getInstance().format(unit);
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
