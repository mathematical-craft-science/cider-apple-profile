package org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * REST anti-corruption layer contract for converting external API measurement units
 * into the service canonical chemical-profile value objects.
 */
public interface ExternalMeasurementConverter<T extends Comparable<? super T>> {

  @NonNull
  T convertValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  );
}
