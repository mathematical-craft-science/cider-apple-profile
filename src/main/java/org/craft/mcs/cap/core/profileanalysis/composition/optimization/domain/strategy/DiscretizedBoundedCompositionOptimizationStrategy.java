package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy;

import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationObjectiveScorer;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationObjectiveAssessment;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationResult;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CalculatedComposition;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculator;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculationRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceWeight;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetIntervalPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
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
 * the supplied {@link CompositionCalculator}, and ranks by objective score.
 * Objective score starts with {@link ProfileTargetDistance} and may include optional criteria such
 * as YAN readiness penalties.
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

    final var rankedCandidates = candidates.stream()
        .sorted(DiscretizedBoundedCompositionOptimizationStrategy::compareByObjective)
        .limit(request.settings().limit())
        .toList();

    return new CompositionOptimizationResult(evaluated[0], rankedCandidates);
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
    final var characteristics = composition.characteristics();
    final var distance = request.objectiveFunction().distanceTo(
        ProfileTargetScoringInput.point(
            characteristics.specificGravity(),
            characteristics.titratableAcidity(),
            characteristics.tannin()
        ),
        ProfileTargetIntervalPolicy.MIDPOINT,
        request.targetObjective()
    );
    objectiveAssessment(request, composition, distance)
        .map(assessment -> new CompositionOptimizationCandidate(
            composition,
            distance,
            assessment
        ))
        .ifPresent(candidates::add);
  }

  private static @NonNull Optional<@NonNull CompositionOptimizationObjectiveAssessment> objectiveAssessment(
      @NonNull CompositionOptimizationRequest request,
      @NonNull CalculatedComposition composition,
      @NonNull ProfileTargetDistance distance
  ) {
    return CompositionOptimizationObjectiveScorer.assess(
        composition,
        distance,
        request.criteria()
    );
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
    return sources;
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

  private static int compareByObjective(
      @NonNull CompositionOptimizationCandidate left,
      @NonNull CompositionOptimizationCandidate right
  ) {
    return Comparator
        .comparing(
            CompositionOptimizationCandidate::objectiveAssessment,
            DiscretizedBoundedCompositionOptimizationStrategy::compareObjectiveAssessments
        )
        .thenComparing(
            CompositionOptimizationCandidate::distance,
            DiscretizedBoundedCompositionOptimizationStrategy::compareDistances
        )
        .compare(left, right);
  }

  private static int compareObjectiveAssessments(
      @NonNull CompositionOptimizationObjectiveAssessment left,
      @NonNull CompositionOptimizationObjectiveAssessment right
  ) {
    final var leftScore = left.score();
    final var rightScore = right.score();
    if (leftScore == null && rightScore == null) {
      return 0;
    }
    if (leftScore == null) {
      return 1;
    }
    if (rightScore == null) {
      return -1;
    }
    return leftScore.compareTo(rightScore);
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
