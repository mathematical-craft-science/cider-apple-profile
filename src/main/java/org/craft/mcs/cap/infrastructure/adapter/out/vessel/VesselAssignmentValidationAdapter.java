package org.craft.mcs.cap.infrastructure.adapter.out.vessel;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.vessel.port.out.FeedstockVesselAssignmentValidator;
import org.craft.mcs.cap.core.production.batch.application.vessel.port.out.ProductionVesselAssignmentValidator;
import org.craft.mcs.cap.core.vessel.application.port.in.ValidateVesselAssignmentCommand;
import org.craft.mcs.cap.core.vessel.application.port.in.ValidateVesselAssignmentUseCase;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** ACL adapter exposing authoritative Vessel assignment validation to material-owning contexts. */
@Component
@RequiredArgsConstructor
public final class VesselAssignmentValidationAdapter
    implements FeedstockVesselAssignmentValidator, ProductionVesselAssignmentValidator {

  private final @NonNull ValidateVesselAssignmentUseCase validateVesselAssignmentUseCase;

  @Override
  public @NonNull VesselReferenceView requireAssignable(
      @NonNull WorkspaceId workspaceId,
      @NonNull VesselId vesselId,
      @NonNull VolumeValue materialVolume
  ) {
    return validateVesselAssignmentUseCase.validate(new ValidateVesselAssignmentCommand(
        vesselId,
        workspaceId,
        materialVolume
    ));
  }
}
