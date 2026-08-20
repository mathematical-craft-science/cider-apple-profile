package org.craft.mcs.cap.core.formulation.optimization.domain.strategy;

import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationSource;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationResult;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationRequest;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculator;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculationRequest;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceWeight;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * Optimizes source compositions by enumerating discrete percentage shares that sum to 100%.
 *
 * <p>The strategy is intentionally exhaustive and explainable for MVP use:
 * with {@code stepPercent = 5} it checks {@code 0%, 5%, 10% ... 100%} shares for each source,
 * constrained so the shares sum to 100. It keeps combinations with at least two selected sources,
 * converts shares according to {@link CompositionOptimizationMode}, calculates each candidate with
 * the supplied {@link CompositionCalculator}. Target scoring and ranking are deliberately outside
 * this search strategy.
 *
 * <p>This algorithm is appropriate when a cider maker selects a small number of feedstock batches
 * and wants an understandable recommendation. It is not a general-purpose solver; future
 * {@link CompositionOptimizationAlgorithm#LINEAR_PROGRAMMING} or
 * {@link CompositionOptimizationAlgorithm#NONLINEAR_OPTIMIZATION} implementations should be selected
 * through {@link CompositionOptimizationStrategySelector} for larger or mathematically richer cases.
 */
public final class DiscretizedBoundedCompositionOptimizationStrategy
    implements CompositionOptimizationStrategy {

  private static final int PERCENT = 100;
  private static final int COMPOSITION_SCALE = 9;

  @Override
  public @NonNull CompositionOptimizationResult optimize(
      @NonNull CompositionOptimizationRequest request,
      @NonNull CompositionCalculator calculator
  ) {
    requireNonNull(request, "composition optimization request cannot be null");
    requireNonNull(calculator, "composition calculator cannot be null");

    final var shares = new int[request.sources().size()];
    final var candidates = new ArrayList<CompositionOptimizationCandidate>();
    final var evaluated = new int[] {0};
    enumerateShares(request, calculator, shares, 0, PERCENT / request.settings().stepPercent(), evaluated, candidates);

    return new CompositionOptimizationResult(evaluated[0], candidates);
  }

  private void enumerateShares(
      @NonNull CompositionOptimizationRequest request,
      @NonNull CompositionCalculator calculator,
      int @NonNull [] shares,
      int index,
      int remainingUnits,
      int @NonNull [] evaluated,
      @NonNull List<@NonNull CompositionOptimizationCandidate> candidates
  ) {
    if (index == shares.length - 1) {
      shares[index] = remainingUnits;
      addCandidate(request, calculator, shares, evaluated, candidates);
      return;
    }

    for (int units = 0; units <= remainingUnits; units++) {
      shares[index] = units;
      enumerateShares(request, calculator, shares, index + 1, remainingUnits - units, evaluated, candidates);
    }
  }

  private void addCandidate(
      @NonNull CompositionOptimizationRequest request,
      @NonNull CompositionCalculator calculator,
      int @NonNull [] shares,
      int @NonNull [] evaluated,
      @NonNull List<@NonNull CompositionOptimizationCandidate> candidates
  ) {
    if (selectedSourceCount(shares) < 2) {
      return;
    }

    final var sources = toCompositionSources(request, shares);
    if (sources.isEmpty()) {
      return;
    }

    evaluated[0]++;
    final var composition = calculator.calculate(new CompositionCalculationRequest(
        sources,
        request.tanninPolicy()
    ));
    candidates.add(new CompositionOptimizationCandidate(composition));
  }

  private static int selectedSourceCount(int @NonNull [] shares) {
    var selected = 0;
    for (final var share : shares) {
      if (share > 0) {
        selected++;
      }
    }
    return selected;
  }

  private static @NonNull List<@NonNull CompositionSource> toCompositionSources(
      @NonNull CompositionOptimizationRequest request,
      int @NonNull [] shares
  ) {
    final var optimizationSources = request.sources();
    if (shares.length != optimizationSources.size()) {
      throw new DomainRuleViolationException("share vector size does not match optimization source count");
    }

    final var sources = new ArrayList<CompositionSource>();
    for (var index = 0; index < shares.length; index++) {
      if (shares[index] == 0) {
        continue;
      }

      // The share vector is generated from the request source list; its index is the source coordinate.
      final var source = optimizationSources.get(index);
      final var weight = weightOf(request, shares[index]);
      if (exceedsAvailableVolume(weight, source)) {
        return List.of();
      }
      sources.add(new CompositionSource(
          source.reference(),
          source.characteristics(),
          weight,
          source.availableVolume()
      ));
    }
    return List.copyOf(sources);
  }

  private static @NonNull CompositionSourceWeight weightOf(
      @NonNull CompositionOptimizationRequest request,
      int shareUnits
  ) {
    final var sharePercent = BigDecimal.valueOf(shareUnits)
        .multiply(BigDecimal.valueOf(request.settings().stepPercent()));
    return switch (request.mode()) {
      case PROPORTION -> new CompositionProportion(
          sharePercent.divide(BigDecimal.valueOf(PERCENT), COMPOSITION_SCALE, RoundingMode.HALF_UP)
      );
      case ABSOLUTE_VOLUME -> new AbsoluteCompositionVolume(
          VolumeValue.ofLiters(absoluteVolumeLiters(request, sharePercent))
      );
    };
  }

  private static @NonNull BigDecimal absoluteVolumeLiters(
      @NonNull CompositionOptimizationRequest request,
      @NonNull BigDecimal sharePercent
  ) {
    final var targetTotalVolume = request.targetTotalVolume();
    if (targetTotalVolume == null) {
      throw new DomainRuleViolationException("absolute volume optimization target total volume is missing");
    }

    return targetTotalVolume.liters()
        .multiply(sharePercent)
        .divide(BigDecimal.valueOf(PERCENT), COMPOSITION_SCALE, RoundingMode.HALF_UP);
  }

  private static boolean exceedsAvailableVolume(
      @NonNull CompositionSourceWeight weight,
      @NonNull CompositionOptimizationSource source
  ) {
    final var availableVolume = source.availableVolume();
    return weight instanceof AbsoluteCompositionVolume(VolumeValue volume)
        && availableVolume != null
        && volume.exceeds(availableVolume);
  }

}
