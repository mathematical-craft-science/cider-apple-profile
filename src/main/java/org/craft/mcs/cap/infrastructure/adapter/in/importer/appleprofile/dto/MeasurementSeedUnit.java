package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto;

import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;

public enum MeasurementSeedUnit {

  SPECIFIC_GRAVITY(BeverageUnits.label(BeverageUnits.SG)),
  PH(BeverageUnits.label(BeverageUnits.PH)),
  GRAM_PER_LITER_AS_MALIC_ACID(BeverageUnits.label(BeverageUnits.TA)),
  MILLIGRAM_PER_LITER_AS_TANNIC_ACID(BeverageUnits.label(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID));

  private final String symbol;

  MeasurementSeedUnit(String symbol) {
    this.symbol = symbol;
  }

  public String symbol() {
    return symbol;
  }
}
