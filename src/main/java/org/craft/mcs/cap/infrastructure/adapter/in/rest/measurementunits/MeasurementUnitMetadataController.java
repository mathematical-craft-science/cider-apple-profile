package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto.MeasurementUnitsMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.mapper.MeasurementUnitMetadataRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Thin REST adapter that exposes CAP canonical unit metadata to API clients.
 *
 * <p>The controller intentionally serves only unit labels and value ranges derived from core
 * value objects. It does not expose graph classifications, target thresholds, or analysis
 * semantics, so the same metadata can be reused by several workspaces.
 */
@RestController
public final class MeasurementUnitMetadataController implements MeasurementUnitMetadataApi {

  private static final @NonNull MeasurementUnitsMetadataResponse METADATA =
      MeasurementUnitMetadataRestMapper.toResponse();

  @Override
  @GetMapping(value = "/measurement-units/metadata", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull MeasurementUnitsMetadataResponse> metadata() {
    return ResponseEntity.ok(METADATA);
  }
}
