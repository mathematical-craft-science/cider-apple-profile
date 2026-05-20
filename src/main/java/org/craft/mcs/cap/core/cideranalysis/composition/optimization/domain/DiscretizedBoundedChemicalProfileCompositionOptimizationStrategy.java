package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculator;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionRequest;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSourceWeight;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Optimizes source compositions by enumerating discrete percentage shares that sum to 100%.
 *
 * <p>The strategy is intentionally exhaustive and explainable for MVP use:
 * with {@code stepPercent = 5} it checks {@code 0%, 5%, 10% ... 100%} shares for each source,
 * constrained so the shares sum to 100. It keeps combinations with at least two selected sources,
 * converts shares according to {@link CompositionOptimizationMode}, calculates each candidate with
 * the supplied {@link ChemicalProfileCompositionCalculator}, and ranks by
 * {@link ProfileTargetDistance}.
 *
 * <p>This algorithm is appropriate when a cider maker selects a small number of feedstock batches
 * and wants an understandable recommendation. It is not a general-purpose solver; future
 * {@link CompositionOptimizationAlgorithm#LINEAR_PROGRAMMING} or
 * {@link CompositionOptimizationAlgorithm#NONLINEAR_OPTIMIZATION} implementations should be selected
 * through {@link CompositionOptimizationStrategySelector} for larger or mathematically richer cases.
 */
public final class DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy
    implements ChemicalProfileCompositionOptimizationStrategy {

  private static final int PERCENT = 100;
  private static final int COMPOSITION_SCALE = 9;

  @Override
  public @NonNull ChemicalProfileCompositionOptimizationResult optimize(
      @NonNull ChemicalProfileCompositionOptimizationRequest request,
      @NonNull ChemicalProfileCompositionCalculator calculator
  ) {
    requireNonNull(request, "chemical profile composition optimization request cannot be null");
    requireNonNull(calculator, "chemical profile composition calculator cannot be null");

    final var shares = new int[request.sources().size()];
    final var candidates = new ArrayList<ChemicalProfileCompositionOptimizationCandidate>();
    final var evaluated = new int[] {0};
    enumerateShares(request, calculator, shares, 0, PERCENT / request.settings().stepPercent(), evaluated, candidates);

    final var rankedCandidates = candidates.stream()
        .sorted(DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy::compareByDistance)
        .limit(request.settings().limit())
        .toList();

    return new ChemicalProfileCompositionOptimizationResult(evaluated[0], rankedCandidates);
  }

  private void enumerateShares(
      @NonNull ChemicalProfileCompositionOptimizationRequest request,
      @NonNull ChemicalProfileCompositionCalculator calculator,
      int @NonNull [] shares,
      int index,
      int remainingUnits,
      int @NonNull [] evaluated,
      @NonNull List<@NonNull ChemicalProfileCompositionOptimizationCandidate> candidates
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
      @NonNull ChemicalProfileCompositionOptimizationRequest request,
      @NonNull ChemicalProfileCompositionCalculator calculator,
      int @NonNull [] shares,
      int @NonNull [] evaluated,
      @NonNull List<@NonNull ChemicalProfileCompositionOptimizationCandidate> candidates
  ) {
    if (selectedSourceCount(shares) < 2) {
      return;
    }

    final var sources = toCompositionSources(request, shares);
    if (sources.isEmpty()) {
      return;
    }

    evaluated[0]++;
    final var composition = calculator.calculate(new ChemicalProfileCompositionRequest(
        sources,
        request.tanninPolicy()
    ));
    final var distance = request.objectiveFunction().distanceTo(composition.profile(), request.targetObjective());
    candidates.add(new ChemicalProfileCompositionOptimizationCandidate(composition, distance));
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

  private static @NonNull List<@NonNull ChemicalProfileCompositionSource> toCompositionSources(
      @NonNull ChemicalProfileCompositionOptimizationRequest request,
      int @NonNull [] shares
  ) {
    final var optimizationSources = request.sources();
    if (shares.length != optimizationSources.size()) {
      throw new IllegalStateException("share vector size does not match optimization source count");
    }

    final var sources = new ArrayList<ChemicalProfileCompositionSource>();
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
      sources.add(new ChemicalProfileCompositionSource(
          source.reference(),
          source.axes(),
          weight,
          source.availableVolume()
      ));
    }
    return sources;
  }

  private static @NonNull ChemicalProfileCompositionSourceWeight weightOf(
      @NonNull ChemicalProfileCompositionOptimizationRequest request,
      int shareUnits
  ) {
    final var sharePercent = BigDecimal.valueOf(shareUnits)
        .multiply(BigDecimal.valueOf(request.settings().stepPercent()));
    return switch (request.mode()) {
      case PROPORTION -> new CompositionProportion(sharePercent);
      case ABSOLUTE_VOLUME -> new AbsoluteCompositionVolume(VolumeValue.ofLiters(absoluteVolumeLiters(request, sharePercent)));
    };
  }

  private static @NonNull BigDecimal absoluteVolumeLiters(
      @NonNull ChemicalProfileCompositionOptimizationRequest request,
      @NonNull BigDecimal sharePercent
  ) {
    if (request.targetTotalVolume() == null) {
      throw new IllegalStateException("absolute volume optimization target total volume is missing");
    }

    return request.targetTotalVolume().liters() // NOSONAR
        .multiply(sharePercent)
        .divide(BigDecimal.valueOf(PERCENT), COMPOSITION_SCALE, RoundingMode.HALF_UP);
  }

  private static boolean exceedsAvailableVolume(
      @NonNull ChemicalProfileCompositionSourceWeight weight,
      @NonNull ChemicalProfileCompositionOptimizationSource source
  ) {
    return weight instanceof AbsoluteCompositionVolume(VolumeValue volume)
        && source.availableVolume() != null
        && volume.compareTo(source.availableVolume()) > 0; // NOSONAR
  }

  private static int compareByDistance(
      @NonNull ChemicalProfileCompositionOptimizationCandidate left,
      @NonNull ChemicalProfileCompositionOptimizationCandidate right
  ) {
    return Comparator
        .comparing(
            ChemicalProfileCompositionOptimizationCandidate::distance,
            DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy::compareDistances
        )
        .compare(left, right);
  }

  private static int compareDistances(
      @NonNull ProfileTargetDistance left,
      @NonNull ProfileTargetDistance right
  ) {
    final var leftValue = left.value();
    final var rightValue = right.value();
    if (leftValue == null && rightValue == null) {
      return 0;
    }
    if (leftValue == null) {
      return 1;
    }
    if (rightValue == null) {
      return -1;
    }
    return leftValue.compareTo(rightValue);
  }
}
