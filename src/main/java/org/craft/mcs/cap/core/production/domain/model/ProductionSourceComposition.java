package org.craft.mcs.cap.core.production.domain.model;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
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
      throw new DomainRuleViolationException("production source composition must contain at least one source");
    }
    final var distinctSourceCount = sources.stream()
        .map(ProductionSourceComponent::reference)
        .distinct()
        .count();
    if (distinctSourceCount != sources.size()) {
      throw new DomainRuleViolationException(
          "production source composition must contain each source exactly once"
      );
    }
    if (totalVolume.liters().compareTo(totalVolumeOf(sources).liters()) != 0) {
      throw new DomainRuleViolationException(
          "production source composition total volume must equal the sum of source volumes"
      );
    }
  }

  public static @NonNull ProductionSourceComposition of(
      @NonNull List<@NonNull ProductionSourceComponent> sources
  ) {
    requireNonNull(sources, "production source composition cannot be null");
    return new ProductionSourceComposition(sources, totalVolumeOf(sources));
  }

  private static @NonNull VolumeValue totalVolumeOf(
      @NonNull List<@NonNull ProductionSourceComponent> sources
  ) {
    var total = BigDecimal.ZERO;
    for (final var source : sources) {
      total = total.add(source.volume().liters());
    }
    return VolumeValue.ofLiters(total);
  }
}
