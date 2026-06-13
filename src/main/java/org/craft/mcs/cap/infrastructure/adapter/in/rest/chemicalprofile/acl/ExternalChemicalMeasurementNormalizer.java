package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * REST anti-corruption layer contract for converting external API measurement units
 * into the service canonical chemical-profile value objects.
 */
public interface ExternalChemicalMeasurementNormalizer<T extends Comparable<T>> {

  @NonNull
  T normalizeValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  );

  @Nullable
  BigDecimal normalizeStandardDeviation(
      @NonNull BigDecimal mean,
      @Nullable BigDecimal standardDeviation,
      @Nullable String unit
  );
}
