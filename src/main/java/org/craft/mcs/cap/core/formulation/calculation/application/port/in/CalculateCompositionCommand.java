package org.craft.mcs.cap.core.formulation.calculation.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Published calculation command composed only of immutable application inputs. */
public record CalculateCompositionCommand(
    @NonNull List<@NonNull CompositionSourceInput> sources,
    @NonNull FormulationTanninPolicy tanninPolicy,
    @Nullable FormulationTargetDefinition target,
    @NonNull FormulationCalculator calculator
) {

  public CalculateCompositionCommand {
    sources = List.copyOf(requireNonNull(sources, "composition sources cannot be null"));
    requireNonNull(tanninPolicy, "composition tannin policy cannot be null");
    requireNonNull(calculator, "composition calculator cannot be null");
  }
}
