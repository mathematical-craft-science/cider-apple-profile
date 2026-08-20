package org.craft.mcs.cap.core.formulation.optimization.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationMode;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationSettings;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationCriterionInput;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationSourceInput;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record OptimizeCompositionCommand(
    @NonNull List<@NonNull OptimizationSourceInput> sources,
    @NonNull FormulationTanninPolicy tanninPolicy,
    @NonNull FormulationTargetDefinition target,
    @NonNull List<@NonNull OptimizationCriterionInput> criteria,
    @NonNull CompositionOptimizationSettings settings,
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull FormulationCalculator calculator,
    @Nullable VolumeValue targetTotalVolume
) {

  public OptimizeCompositionCommand {
    sources = List.copyOf(requireNonNull(sources, "composition optimization sources cannot be null"));
    requireNonNull(tanninPolicy, "composition tannin policy cannot be null");
    requireNonNull(target, "formulation target cannot be null");
    criteria = List.copyOf(requireNonNull(criteria, "composition optimization criteria cannot be null"));
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculator, "composition calculator cannot be null");
  }
}
