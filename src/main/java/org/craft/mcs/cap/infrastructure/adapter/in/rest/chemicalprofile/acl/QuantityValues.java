package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl;

import java.math.BigDecimal;
import javax.measure.Quantity;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


final class QuantityValues {

  private QuantityValues() {
  }

  static @NonNull BigDecimal bigDecimal(@NonNull Quantity<?> quantity) {
    requireNonNull(quantity, "quantity cannot be null");
    final var value = quantity.getValue();
    if (value instanceof BigDecimal bigDecimal) {
      return bigDecimal;
    }
    return new BigDecimal(value.toString());
  }
}
