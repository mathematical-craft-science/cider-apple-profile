package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record OptimizeFeedstockBatchCompositionCommand(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockMeasurementPointFilters filters,
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull FeedstockBatchBlendProfileTargetSelection target,
    @Nullable TanninTargetRange tanninTarget,
    @NonNull ProfileTargetObjectiveWeights weights,
    @NonNull DiscretizedCompositionOptimizationSettings settings,
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode,
    @Nullable VolumeValue targetTotalVolume,
    int sourceLimit
) {

  public OptimizeFeedstockBatchCompositionCommand {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(target, "feedstock batch blend profile target cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculatorCode, "chemical profile composition calculator code cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "profile target objective function code cannot be null");

    if (sourceLimit < 2 || sourceLimit > CursorPageRequest.MAX_LIMIT) {
      throw new IllegalArgumentException("composition optimization source limit must be between 2 and " + CursorPageRequest.MAX_LIMIT);
    }
  }
}
