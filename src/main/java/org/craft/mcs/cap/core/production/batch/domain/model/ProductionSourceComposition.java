package org.craft.mcs.cap.core.production.batch.domain.model;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Confirmed source components that define the initial material of a ProductionBatch.
 *
 * <p>The component list is the only source of truth; total volume is always derived from it.</p>
 */
@ValueObject
public record ProductionSourceComposition(
    @NonNull List<@NonNull ProductionSourceComponent> sources
) {

  public ProductionSourceComposition {
    sources = List.copyOf(requireNonNull(sources, "production source composition cannot be null"));

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
  }

  public static @NonNull ProductionSourceComposition of(
      @NonNull List<@NonNull ProductionSourceComponent> sources
  ) {
    return new ProductionSourceComposition(sources);
  }

  /**
   * Returns the authoritative total derived from all source components.
   */
  public @NonNull VolumeValue totalVolume() {
    return totalVolumeOf(sources);
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
