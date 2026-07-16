package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper.ChemicalProfileCompositionCalculationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper.ChemicalProfileCompositionOptimizationRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Resolves REST target specifications while mapping composition requests to application commands. */
@Component
@RequiredArgsConstructor
public final class ChemicalProfileCompositionRequestMapper {

  private final @NonNull SugarAcidityTargetResolver targetResolver;

  @NonNull CalculateChemicalProfileCompositionCommand toCalculationCommand(
      @NonNull CalculateChemicalProfileCompositionRequest request
  ) {
    return ChemicalProfileCompositionCalculationRestMapper.toCommand(request, targetResolver);
  }

  @NonNull OptimizeChemicalProfileCompositionCommand toOptimizationCommand(
      @NonNull OptimizeChemicalProfileCompositionRequest request
  ) {
    return ChemicalProfileCompositionOptimizationRestMapper.toCommand(request, targetResolver);
  }
}
