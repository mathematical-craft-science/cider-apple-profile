package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.ChemicalProfileCompositionOptimizationSource;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record OptimizeChemicalProfileCompositionCommand(
    @NonNull List<@NonNull ChemicalProfileCompositionOptimizationSource> sources,
    @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @NonNull ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetObjectiveWeights targetWeights,
    @NonNull ProfileTargetScoringPurpose scoringPurpose,
    @NonNull DiscretizedCompositionOptimizationSettings settings,
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode,
    @Nullable VolumeValue targetTotalVolume
) {

  public OptimizeChemicalProfileCompositionCommand {
    sources = List.copyOf(requireNonNull(sources, "chemical profile composition optimization sources cannot be null"));
    requireNonNull(tanninPolicy, "chemical profile composition tannin policy cannot be null");
    requireNonNull(targetObjective, "profile target objective cannot be null");
    requireNonNull(targetWeights, "profile target objective weights cannot be null");
    requireNonNull(scoringPurpose, "profile target scoring purpose cannot be null");
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculatorCode, "chemical profile composition calculator code cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "profile target objective function code cannot be null");
  }
}
