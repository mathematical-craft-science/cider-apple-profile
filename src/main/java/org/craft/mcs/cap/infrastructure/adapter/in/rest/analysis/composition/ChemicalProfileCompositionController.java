package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper.ChemicalProfileCompositionCalculationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper.ChemicalProfileCompositionOptimizationRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class ChemicalProfileCompositionController implements ChemicalProfileCompositionApi {

  private final @NonNull CalculateChemicalProfileCompositionUseCase calculateChemicalProfileCompositionUseCase;
  private final @NonNull OptimizeChemicalProfileCompositionUseCase optimizeChemicalProfileCompositionUseCase;
  private final @NonNull ChemicalProfileCompositionRequestMapper requestMapper;

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
        requestMapper.toCalculationCommand(request)
    );

    return ResponseEntity.ok(
        ChemicalProfileCompositionCalculationRestMapper.toResponse(result)
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
        requestMapper.toOptimizationCommand(request)
    );

    return ResponseEntity.ok(
        ChemicalProfileCompositionOptimizationRestMapper.toResponse(result, stepPercent(request))
    );
  }

  private static int stepPercent(@NonNull OptimizeChemicalProfileCompositionRequest request) {
    return requireNonNullElse( // NOSONAR
        request.stepPercent(),
        DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT
    );
  }
}
