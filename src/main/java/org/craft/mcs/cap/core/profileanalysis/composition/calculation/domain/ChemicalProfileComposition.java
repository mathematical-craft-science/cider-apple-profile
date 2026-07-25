package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
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
    @Nullable AxisValue yeastAssimilableNitrogen,
    @Nullable VolumeValue totalVolume,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
) {

  public ChemicalProfileComposition {
    sources = List.copyOf(requireNonNull(sources, "chemical profile composition sources cannot be null"));
    requireNonNull(profile, "chemical profile composition profile cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "chemical profile composition warnings cannot be null"));

    if (sources.isEmpty()) {
      throw new InvalidChemicalProfileCompositionException(
          "chemical profile composition must contain at least one source"
      );
    }
    requireUniqueSources(sources);
    requireConsistentWeightMode(sources);
    requireConsistentTotalVolume(sources, totalVolume);
    requireYeastAssimilableNitrogenAxis(yeastAssimilableNitrogen);
  }

  public static @NonNull ChemicalProfileComposition of(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @NonNull SugarAcidityProfilePoint profile,
      @Nullable AxisValue yeastAssimilableNitrogen,
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

  private static void requireUniqueSources(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources
  ) {
    if (sources.stream().map(ChemicalProfileCompositionSource::reference).distinct().count() != sources.size()) {
      throw new InvalidChemicalProfileCompositionException(
          "calculated composition must contain each source exactly once"
      );
    }
  }

  private static void requireConsistentWeightMode(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources
  ) {
    final var weightType = sources.getFirst().weight().getClass();
    if (sources.stream().anyMatch(source -> source.weight().getClass() != weightType)) {
      throw new InvalidChemicalProfileCompositionException(
          "calculated composition cannot mix absolute volumes and proportions"
      );
    }
  }

  private static void requireConsistentTotalVolume(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @Nullable VolumeValue declaredTotalVolume
  ) {
    final var calculatedTotalVolume = totalVolume(sources);
    if (calculatedTotalVolume == null && declaredTotalVolume != null) {
      throw new InvalidChemicalProfileCompositionException(
          "proportion-based composition cannot declare a concrete total volume"
      );
    }
    if (calculatedTotalVolume != null
        && (declaredTotalVolume == null || !calculatedTotalVolume.equals(declaredTotalVolume))) {
      throw new InvalidChemicalProfileCompositionException(
          "composition total volume must equal the sum of absolute source volumes"
      );
    }
  }

  private static void requireYeastAssimilableNitrogenAxis(
      @Nullable AxisValue yeastAssimilableNitrogen
  ) {
    if (yeastAssimilableNitrogen != null
        && yeastAssimilableNitrogen.axis() != ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN) {
      throw new InvalidChemicalProfileCompositionException(
          "composition yeast assimilable nitrogen must use YEAST_ASSIMILABLE_NITROGEN axis"
      );
    }
  }
}
