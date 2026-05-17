package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.OptimizeChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.OptimizeChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.mapper.ChemicalProfileCompositionRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class ChemicalProfileCompositionController implements ChemicalProfileCompositionApi {

  private final @NonNull CalculateChemicalProfileCompositionUseCase calculateChemicalProfileCompositionUseCase;
  private final @NonNull OptimizeChemicalProfileCompositionUseCase optimizeChemicalProfileCompositionUseCase;
  private final @NonNull SugarAcidityTargetResolver targetResolver;

  @Override
  @PostMapping(
      value = "/chemical-profile-compositions/calculations",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull ChemicalProfileCompositionResponse> calculate(
      @RequestBody @NonNull CalculateChemicalProfileCompositionRequest request
  ) {
    final var result = calculateChemicalProfileCompositionUseCase.calculate(
        ChemicalProfileCompositionRestMapper.toCommand(request, targetResolver)
    );

    return ResponseEntity.ok(
        ChemicalProfileCompositionRestMapper.toResponse(result)
    );
  }

  @Override
  @PostMapping(
      value = "/chemical-profile-compositions/optimizations",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull OptimizeChemicalProfileCompositionResponse> optimize(
      @RequestBody @NonNull OptimizeChemicalProfileCompositionRequest request
  ) {
    final var result = optimizeChemicalProfileCompositionUseCase.optimize(
        ChemicalProfileCompositionRestMapper.toOptimizationCommand(request, targetResolver)
    );

    return ResponseEntity.ok(
        ChemicalProfileCompositionRestMapper.toResponse(result, stepPercent(request))
    );
  }

  private static int stepPercent(@NonNull OptimizeChemicalProfileCompositionRequest request) {
    return request.stepPercent() == null
        ? DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT
        : request.stepPercent();
  }
}
