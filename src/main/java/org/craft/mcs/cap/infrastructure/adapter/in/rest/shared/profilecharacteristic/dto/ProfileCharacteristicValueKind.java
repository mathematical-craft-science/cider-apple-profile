package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto;


/** Identifies the representation exposed for a characteristic value in REST responses. */
public enum ProfileCharacteristicValueKind {

  /** One retained point value. */
  POINT,

  /** Interval bounds accompanied by their representative midpoint. */
  INTERVAL_MIDPOINT,

  /** Point value calculated from source characteristics. */
  CALCULATED
}
