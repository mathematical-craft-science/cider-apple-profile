package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.mapper;

import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.vessel.application.contract.VesselCurrentContentView;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.port.in.CreateVesselCommand;
import org.craft.mcs.cap.core.vessel.application.port.in.ListAvailableVesselsQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.UpdateVesselCommand;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.CreateVesselRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.UpdateVesselRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselCurrentContentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselReferenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselResponse;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Maps vessel transport DTOs to application commands and read responses. */
public final class VesselRestMapper {

  private VesselRestMapper() {
  }

  public static @NonNull CreateVesselCommand toCommand(@NonNull CreateVesselRequest request) {
    requireNonNull(request, "create vessel request cannot be null");
    return new CreateVesselCommand(
        WorkspaceId.of(requireNonNull(request.workspaceId(), "workspace id cannot be null")),
        request.name(),
        VolumeValue.ofLiters(request.nominalCapacityLiters()),
        request.material()
    );
  }

  public static @NonNull UpdateVesselCommand toCommand(
      @NonNull VesselId vesselId,
      @NonNull UpdateVesselRequest request
  ) {
    requireNonNull(vesselId, "vessel id cannot be null");
    requireNonNull(request, "update vessel request cannot be null");
    return new UpdateVesselCommand(
        vesselId,
        WorkspaceId.of(requireNonNull(request.workspaceId(), "workspace id cannot be null")),
        AggregateRevision.of(requireNonNull(request.expectedVersion(), "expected vessel version cannot be null")),
        request.name(),
        VolumeValue.ofLiters(request.nominalCapacityLiters()),
        request.material(),
        request.operationalStatus()
    );
  }

  public static @NonNull ListAvailableVesselsQuery toAvailableQuery(
      @NonNull UUID workspaceId,
      @Nullable String nameContains,
      @Nullable BigDecimal requiredVolumeLiters
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    final var nameCriteria = nameContains == null || nameContains.isBlank()
        ? null
        : new TextSearchCriteria(nameContains);
    final var requiredVolume = requiredVolumeLiters == null
        ? null
        : VolumeValue.ofLiters(requiredVolumeLiters);
    return new ListAvailableVesselsQuery(
        WorkspaceId.of(workspaceId),
        nameCriteria,
        requiredVolume
    );
  }

  public static @NonNull VesselResponse toResponse(@NonNull VesselUsageView view) {
    requireNonNull(view, "vessel view cannot be null");
    final var vessel = view.vessel();
    return new VesselResponse(
        vessel.vesselId().value(),
        vessel.workspaceId().value(),
        vessel.name(),
        toVolumeResponse(vessel.nominalCapacity()),
        vessel.material(),
        vessel.operationalStatus(),
        toResponse(view.currentContent()),
        toVolumeResponse(view.usedVolume()),
        toVolumeResponse(view.remainingCapacity()),
        view.usage(),
        vessel.revision().value()
    );
  }

  public static @NonNull VesselReferenceResponse toResponse(@NonNull VesselReferenceView view) {
    requireNonNull(view, "vessel reference view cannot be null");
    return new VesselReferenceResponse(view.vesselId().value(), view.name());
  }

  private static @Nullable VesselCurrentContentResponse toResponse(
      @Nullable VesselCurrentContentView currentContent
  ) {
    if (currentContent == null) {
      return null;
    }
    return new VesselCurrentContentResponse(
        currentContent.subjectType(),
        currentContent.subjectId(),
        currentContent.materialStateId().value(),
        currentContent.displayName().value(),
        toVolumeResponse(currentContent.volume())
    );
  }

  private static @NonNull DecimalQuantityResponse toVolumeResponse(@NonNull VolumeValue volume) {
    return new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
  }
}
