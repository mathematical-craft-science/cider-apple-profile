package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in.AssessAppleVarietySugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in.QueryAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper.AppleVarietyProfileGraphRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class AppleVarietySugarAcidityController implements AppleVarietySugarAcidityApi {

  private final @NonNull AssessAppleVarietySugarAcidityZoneUseCase assessAppleVarietySugarAcidityZoneUseCase;
  private final @NonNull QueryAppleVarietySugarAcidityZonePointsUseCase queryAppleVarietySugarAcidityZonePointsUseCase;
  private final @NonNull AppleVarietyProfileGraphRestMapper restMapper;

  @Override
  @GetMapping(value = "/apple-varieties/{varietyId}/sugar-acidity-zone", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AppleVarietySugarAcidityZoneResponse> assessSugarAcidityZone(
      @PathVariable @NonNull UUID varietyId
  ) {
    final var result = assessAppleVarietySugarAcidityZoneUseCase.assess(
        AppleVarietyId.of(varietyId)
    );

    return ResponseEntity.ok(
        AppleVarietyProfileGraphRestMapper.toResponse(result)
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
    final var query = restMapper.toQuery(request);
    final var results = queryAppleVarietySugarAcidityZonePointsUseCase.query(query);

    return ResponseEntity.ok(
        restMapper.toResponse(results, query)
    );
  }
}
