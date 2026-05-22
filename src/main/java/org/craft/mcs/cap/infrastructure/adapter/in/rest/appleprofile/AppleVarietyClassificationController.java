package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AssessAppleVarietySugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.in.ClassifyAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.in.ClassifyAppleVarietyNorthAmericanUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.in.PredictAppleVarietyPotentialAbvUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.in.QueryAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietyClassificationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietyNorthAmericanResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietyPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper.AppleVarietyClassificationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper.AppleVarietyNorthAmericanRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper.AppleVarietyPotentialAbvRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper.AppleVarietySugarAcidityZoneRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class AppleVarietyClassificationController implements AppleVarietyClassificationApi {

  private final @NonNull ClassifyAppleVarietyUseCase classifyAppleVarietyUseCase;
  private final @NonNull ClassifyAppleVarietyNorthAmericanUseCase classifyAppleVarietyNorthAmericanUseCase;
  private final @NonNull AssessAppleVarietySugarAcidityZoneUseCase assessAppleVarietySugarAcidityZoneUseCase;
  private final @NonNull PredictAppleVarietyPotentialAbvUseCase predictAppleVarietyPotentialAbvUseCase;
  private final @NonNull QueryAppleVarietySugarAcidityZonePointsUseCase queryAppleVarietySugarAcidityZonePointsUseCase;

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

  @Override
  @GetMapping(value = "/apple-varieties/{varietyId}/sugar-acidity-zone", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AppleVarietySugarAcidityZoneResponse> assessSugarAcidityZone(
      @PathVariable @NonNull UUID varietyId
  ) {
    final var result = assessAppleVarietySugarAcidityZoneUseCase.assess(
        AppleVarietyId.of(varietyId)
    );

    return ResponseEntity.ok(
        AppleVarietySugarAcidityZoneRestMapper.toResponse(result)
    );
  }

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

  @Override
  @PostMapping(
      value = "/apple-varieties/sugar-acidity-zone-points",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull AppleVarietySugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @RequestBody @NonNull AppleVarietySugarAcidityZonePointQueryRequest request
  ) {
    final var results = queryAppleVarietySugarAcidityZonePointsUseCase.query(
        AppleVarietySugarAcidityZoneRestMapper.toQuery(request)
    );

    return ResponseEntity.ok(
        AppleVarietySugarAcidityZoneRestMapper.toResponse(results)
    );
  }
}
