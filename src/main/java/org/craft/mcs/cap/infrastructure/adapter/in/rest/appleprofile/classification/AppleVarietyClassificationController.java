package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.in.ClassifyAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.in.ClassifyAppleVarietyNorthAmericanUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto.AppleVarietyClassificationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto.AppleVarietyNorthAmericanResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.mapper.AppleVarietyClassificationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.mapper.AppleVarietyNorthAmericanRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class AppleVarietyClassificationController implements AppleVarietyClassificationApi {

  private final @NonNull ClassifyAppleVarietyUseCase classifyAppleVarietyUseCase;
  private final @NonNull ClassifyAppleVarietyNorthAmericanUseCase classifyAppleVarietyNorthAmericanUseCase;

  @Override
  @GetMapping(value = "/apple-varieties/{varietyId}/classification", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AppleVarietyClassificationResponse> classify(
      @PathVariable @NonNull UUID varietyId,
      @RequestParam @NonNull AppleVarietyClassificationSystem system
  ) {
    final var classification = classifyAppleVarietyUseCase.classify(
        AppleVarietyId.of(varietyId),
        system
    );

    return ResponseEntity.ok(
        AppleVarietyClassificationRestMapper.toResponse(classification)
    );
  }

  @Override
  @GetMapping(value = "/apple-varieties/{varietyId}/north-american", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AppleVarietyNorthAmericanResponse> classifyNorthAmerican(
      @PathVariable @NonNull UUID varietyId
  ) {
    final var result = classifyAppleVarietyNorthAmericanUseCase.classify(
        AppleVarietyId.of(varietyId)
    );

    return ResponseEntity.ok(
        AppleVarietyNorthAmericanRestMapper.toResponse(result)
    );
  }
}
