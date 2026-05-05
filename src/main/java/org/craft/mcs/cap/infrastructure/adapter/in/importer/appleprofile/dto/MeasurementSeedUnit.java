package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto;

public enum MeasurementSeedUnit {

  SPECIFIC_GRAVITY("SG"),
  PH("pH"),
  GRAM_PER_LITER_AS_MALIC_ACID("g/L as malic acid"),
  MILLIGRAM_PER_LITER_AS_TANNIC_ACID("mg/L TAE");

  private final String symbol;

  MeasurementSeedUnit(String symbol) {
    this.symbol = symbol;
  }

  public String symbol() {
    return symbol;
  }
}
