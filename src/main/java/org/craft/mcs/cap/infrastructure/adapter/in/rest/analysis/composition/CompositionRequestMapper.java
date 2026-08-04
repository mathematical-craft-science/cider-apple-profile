package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper.CompositionCalculationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper.CompositionOptimizationRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Resolves REST target specifications while mapping composition requests to application commands. */
@Component
@RequiredArgsConstructor
public final class CompositionRequestMapper {

  private final @NonNull SugarAcidityTargetResolver targetResolver;

  @NonNull CalculateCompositionCommand toCalculationCommand(
      @NonNull CalculateCompositionRequest request
  ) {
    return CompositionCalculationRestMapper.toCommand(request, targetResolver);
  }

  @NonNull OptimizeCompositionCommand toOptimizationCommand(
      @NonNull OptimizeCompositionRequest request
  ) {
    return CompositionOptimizationRestMapper.toCommand(request, targetResolver);
  }
}
