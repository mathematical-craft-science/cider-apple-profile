package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition;

import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper.CompositionCalculationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper.CompositionOptimizationRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Maps composition REST requests to published Formulation commands. */
@Component
public final class CompositionRequestMapper {

  @NonNull CalculateCompositionCommand toCalculationCommand(
      @NonNull CalculateCompositionRequest request
  ) {
    return CompositionCalculationRestMapper.toCommand(request);
  }

  @NonNull OptimizeCompositionCommand toOptimizationCommand(
      @NonNull OptimizeCompositionRequest request
  ) {
    return CompositionOptimizationRestMapper.toCommand(request);
  }
}
