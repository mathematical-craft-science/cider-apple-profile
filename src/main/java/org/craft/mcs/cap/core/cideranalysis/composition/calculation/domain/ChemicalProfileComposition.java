package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Calculated chemical profile of a source composition.
 *
 * <p>The composition keeps the exact sources used for calculation, the resulting profile point,
 * optional total volume when source weights are concrete volumes, and warnings for intentionally
 * partial axes such as incomplete tannin or YAN.
 */
public record ChemicalProfileComposition(
    @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
    @NonNull SugarAcidityProfilePoint profile,
    @Nullable SugarAcidityAxisPoint yeastAssimilableNitrogen,
    @Nullable VolumeValue totalVolume,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
) {

  public ChemicalProfileComposition {
    sources = List.copyOf(requireNonNull(sources, "chemical profile composition sources cannot be null"));
    requireNonNull(profile, "chemical profile composition profile cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "chemical profile composition warnings cannot be null"));

    if (sources.isEmpty()) {
      throw new InvalidChemicalProfileCompositionException("chemical profile composition must contain at least one source");
    }
  }

  public static @NonNull ChemicalProfileComposition of(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @NonNull SugarAcidityProfilePoint profile,
      @Nullable SugarAcidityAxisPoint yeastAssimilableNitrogen,
      @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
  ) {
    return new ChemicalProfileComposition(
        sources,
        profile,
        yeastAssimilableNitrogen,
        totalVolume(sources),
        warnings
    );
  }

  public boolean hasConcreteVolumes() {
    return totalVolume != null;
  }

  private static @Nullable VolumeValue totalVolume(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources
  ) {
    var total = BigDecimal.ZERO;

    for (final var source : sources) {
      if (!(source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume))) {
        return null;
      }
      total = total.add(volume.liters());
    }

    return VolumeValue.ofLiters(total);
  }
}
