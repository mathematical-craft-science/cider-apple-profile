package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionUseCase;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionUseCase;
import org.craft.mcs.cap.core.formulation.interpretation.application.port.in.CompositionGraphInterpretationUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculatedCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper.CompositionCalculationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper.CompositionOptimizationRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class CompositionController implements CompositionApi {

  private final @NonNull CalculateCompositionUseCase calculateCompositionUseCase;
  private final @NonNull OptimizeCompositionUseCase optimizeCompositionUseCase;
  private final @NonNull CompositionGraphInterpretationUseCase graphInterpretationUseCase;
  private final @NonNull CompositionRequestMapper requestMapper;

  @Override
  @PostMapping(
      value = "/composition-calculations",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull CalculatedCompositionResponse> calculate(
      @RequestBody @NonNull CalculateCompositionRequest request
  ) {
    final var result = calculateCompositionUseCase.calculate(
        requestMapper.toCalculationCommand(request)
    );

    return ResponseEntity.ok(
        CompositionCalculationRestMapper.toResponse(graphInterpretationUseCase.interpret(result))
    );
  }

  @Override
  @PostMapping(
      value = "/composition-optimizations",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull OptimizeCompositionResponse> optimize(
      @RequestBody @NonNull OptimizeCompositionRequest request
  ) {
    final var result = optimizeCompositionUseCase.optimize(
        requestMapper.toOptimizationCommand(request)
    );

    return ResponseEntity.ok(
        CompositionOptimizationRestMapper.toResponse(graphInterpretationUseCase.interpret(result))
    );
  }
}
