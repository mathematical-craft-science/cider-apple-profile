package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.port.in.CreateVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.GetVesselQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.GetVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.ListAvailableVesselsUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.ListVesselsQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ListVesselsUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.UpdateVesselUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.CreateVesselRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.UpdateVesselRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.mapper.VesselRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** Thin HTTP adapter for Vessel catalog use cases. */
@RestController
@RequiredArgsConstructor
public final class VesselController implements VesselApi {

  private final @NonNull CreateVesselUseCase createVesselUseCase;
  private final @NonNull GetVesselUseCase getVesselUseCase;
  private final @NonNull ListAvailableVesselsUseCase listAvailableVesselsUseCase;
  private final @NonNull ListVesselsUseCase listVesselsUseCase;
  private final @NonNull UpdateVesselUseCase updateVesselUseCase;

  @Override
  @PostMapping(value = "/vessels", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull VesselResponse> create(
      @RequestBody @NonNull CreateVesselRequest request
  ) {
    final var created = createVesselUseCase.create(VesselRestMapper.toCommand(request));
    return ResponseEntity.created(URI.create("/vessels/" + created.vessel().vesselId().value()))
        .body(VesselRestMapper.toResponse(created));
  }

  @Override
  @GetMapping(value = "/vessels", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull VesselListResponse> list(
      @RequestParam @NonNull UUID workspaceId
  ) {
    final var vessels = listVesselsUseCase.list(new ListVesselsQuery(WorkspaceId.of(workspaceId)));
    return ResponseEntity.ok(new VesselListResponse(
        vessels.stream().map(VesselRestMapper::toResponse).toList()
    ));
  }

  @Override
  @GetMapping(value = "/vessels/available", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull VesselListResponse> listAvailable(
      @RequestParam @NonNull UUID workspaceId,
      @RequestParam(required = false) @Nullable String nameContains,
      @RequestParam(required = false) @Nullable BigDecimal requiredVolumeLiters
  ) {
    final var vessels = listAvailableVesselsUseCase.listAvailable(
        VesselRestMapper.toAvailableQuery(workspaceId, nameContains, requiredVolumeLiters)
    );
    return ResponseEntity.ok(new VesselListResponse(
        vessels.stream().map(VesselRestMapper::toResponse).toList()
    ));
  }

  @Override
  @GetMapping(value = "/vessels/{vesselId}", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull VesselResponse> get(
      @PathVariable @NonNull UUID vesselId,
      @RequestParam @NonNull UUID workspaceId
  ) {
    final var vessel = getVesselUseCase.get(new GetVesselQuery(
        VesselId.of(vesselId),
        WorkspaceId.of(workspaceId)
    ));
    return ResponseEntity.ok(VesselRestMapper.toResponse(vessel));
  }

  @Override
  @PutMapping(
      value = "/vessels/{vesselId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull VesselResponse> update(
      @PathVariable @NonNull UUID vesselId,
      @RequestBody @NonNull UpdateVesselRequest request
  ) {
    final var vessel = updateVesselUseCase.update(
        VesselRestMapper.toCommand(VesselId.of(vesselId), request)
    );
    return ResponseEntity.ok(VesselRestMapper.toResponse(vessel));
  }
}
