package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietyClassificationResponse;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.appleprofile.application.port.in.ClassifyAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper.AppleVarietyClassificationRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public final class AppleVarietyClassificationController {

  private final @NonNull ClassifyAppleVarietyUseCase classifyAppleVarietyUseCase;

  @GetMapping("/apple-varieties/{varietyId}/classification")
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
}
