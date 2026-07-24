package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * Stable transport code for a canonical CAP unit exposed by the measurement metadata endpoint.
 *
 * <p>These values are intentionally unit-oriented, not analysis-axis-oriented: clients use them to
 * configure inputs and labels without knowing which business scenario will consume the value.
 */
@Schema(description = "CAP canonical measurement unit code.")
public enum MeasurementUnitCode {
  SG,
  TA,
  TANNIN,
  YAN,
  PH,
  ABV,
  VOLUME
}
