package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationCriterion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record OptimizeCompositionCommand(
    @NonNull List<@NonNull CompositionOptimizationSource> sources,
    @NonNull CompositionTanninPolicy tanninPolicy,
    @NonNull ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetScoringConfiguration scoringConfiguration,
    @NonNull List<@NonNull CompositionOptimizationCriterion> criteria,
    @NonNull DiscretizedCompositionOptimizationSettings settings,
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull CompositionCalculatorCode calculatorCode,
    @Nullable VolumeValue targetTotalVolume
) {

  public OptimizeCompositionCommand {
    sources = List.copyOf(requireNonNull(sources, "composition optimization sources cannot be null"));
    requireNonNull(tanninPolicy, "composition tannin policy cannot be null");
    requireNonNull(targetObjective, "profile target objective cannot be null");
    requireNonNull(scoringConfiguration, "profile target scoring configuration cannot be null");
    criteria = List.copyOf(requireNonNull(criteria, "composition optimization criteria cannot be null"));
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculatorCode, "composition calculator code cannot be null");
  }
}
