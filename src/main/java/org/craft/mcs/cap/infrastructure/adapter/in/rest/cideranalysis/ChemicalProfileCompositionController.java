package org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.CalculateChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto.ChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.mapper.ChemicalProfileCompositionRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class ChemicalProfileCompositionController {

  private final @NonNull CalculateChemicalProfileCompositionUseCase calculateChemicalProfileCompositionUseCase;
  private final @NonNull SugarAcidityTargetResolver targetResolver;

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
}
