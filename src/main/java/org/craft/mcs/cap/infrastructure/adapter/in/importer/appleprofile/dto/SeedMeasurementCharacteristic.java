package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto;

public enum SeedMeasurementCharacteristic {

  SUGAR(MeasurementSeedUnit.SPECIFIC_GRAVITY),
  PH(MeasurementSeedUnit.PH),
  TITRATABLE_ACIDITY(MeasurementSeedUnit.GRAM_PER_LITER_AS_MALIC_ACID),
  TANNINITY(MeasurementSeedUnit.MILLIGRAM_PER_LITER_AS_TANNIC_ACID);

  private final MeasurementSeedUnit canonicalUnit;

  SeedMeasurementCharacteristic(MeasurementSeedUnit canonicalUnit) {
    this.canonicalUnit = canonicalUnit;
  }

  public MeasurementSeedUnit canonicalUnit() {
    return canonicalUnit;
  }

  public void validateUnit(MeasurementSeedUnit actualUnit) {
    if (actualUnit != canonicalUnit) {
      throw new IllegalArgumentException(
          "Invalid seed unit for " + name()
              + ": expected " + canonicalUnit.symbol()
              + ", got " + actualUnit.symbol()
      );
    }
  }
}
