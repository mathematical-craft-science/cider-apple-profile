package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;


public class MeasurementJdbcColumns {

  @Nullable
  public MeasurementKind kind;

  @Nullable
  public BigDecimal exactValue;

  @Nullable
  public BigDecimal minValue;

  @Nullable
  public BigDecimal maxValue;

  public MeasurementJdbcColumns() {
  }
}
