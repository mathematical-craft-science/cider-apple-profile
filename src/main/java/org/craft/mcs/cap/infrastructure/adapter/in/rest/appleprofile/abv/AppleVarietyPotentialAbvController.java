package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.abv;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.in.PredictAppleVarietyPotentialAbvUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.abv.dto.AppleVarietyPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.abv.mapper.AppleVarietyPotentialAbvRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class AppleVarietyPotentialAbvController implements AppleVarietyPotentialAbvApi {

  private final @NonNull PredictAppleVarietyPotentialAbvUseCase predictAppleVarietyPotentialAbvUseCase;

  @Override
  @GetMapping(value = "/apple-varieties/{varietyId}/abv-prediction", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AppleVarietyPotentialAbvResponse> predictPotentialAbv(
      @PathVariable @NonNull UUID varietyId
  ) {
    final var result = predictAppleVarietyPotentialAbvUseCase.predict(
        AppleVarietyId.of(varietyId)
    );

    return ResponseEntity.ok(
        AppleVarietyPotentialAbvRestMapper.toResponse(result)
    );
  }
}
