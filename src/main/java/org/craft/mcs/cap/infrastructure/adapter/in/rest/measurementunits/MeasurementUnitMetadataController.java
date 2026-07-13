package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsReader;
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
 * <p>The controller serves reusable input/display metadata derived from core value objects and
 * current workspace settings. It does not expose graph classifications, target thresholds, or
 * analysis semantics.
 */
@RestController
@RequiredArgsConstructor
public final class MeasurementUnitMetadataController implements MeasurementUnitMetadataApi {

  private final @NonNull WorkspaceSettingsReader workspaceSettingsReader;

  @Override
  @GetMapping(value = "/measurement-units/metadata", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull MeasurementUnitsMetadataResponse> metadata() {
    final var workspaceSettings = workspaceSettingsReader.readCurrent();
    return ResponseEntity.ok(MeasurementUnitMetadataRestMapper.toResponse(
        workspaceSettings.measurementStaleAfter()
    ));
  }
}
