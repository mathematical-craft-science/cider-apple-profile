package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;


/** Reusable JDBC columns for a point or interval characteristic value. */
public final class CharacteristicValueJdbcColumns {

  public @Nullable CharacteristicValueStorageKind kind;
  public @Nullable BigDecimal exactValue;
  public @Nullable BigDecimal minValue;
  public @Nullable BigDecimal maxValue;

  public CharacteristicValueJdbcColumns() {
  }
}
