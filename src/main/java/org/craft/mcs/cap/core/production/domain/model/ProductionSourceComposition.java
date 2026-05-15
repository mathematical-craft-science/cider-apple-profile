package org.craft.mcs.cap.core.production.domain.model;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record ProductionSourceComposition(
    @NonNull List<@NonNull ProductionSourceComponent> sources,
    @NonNull VolumeValue totalVolume
) {

  public ProductionSourceComposition {
    sources = List.copyOf(requireNonNull(sources, "production source composition cannot be null"));
    requireNonNull(totalVolume, "production source composition total volume cannot be null");

    if (sources.isEmpty()) {
      throw new IllegalArgumentException("production source composition must contain at least one source");
    }
  }

  public static @NonNull ProductionSourceComposition of(
      @NonNull List<@NonNull ProductionSourceComponent> sources
  ) {
    requireNonNull(sources, "production source composition cannot be null");

    var total = BigDecimal.ZERO;
    for (final var source : sources) {
      total = total.add(source.volume().liters());
    }

    return new ProductionSourceComposition(sources, VolumeValue.ofLiters(total));
  }
}
