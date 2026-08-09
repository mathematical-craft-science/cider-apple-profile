package org.craft.mcs.cap.core.vessel.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.VesselNotFoundException;
import org.craft.mcs.cap.core.vessel.application.VesselOccupiedException;
import org.craft.mcs.cap.core.vessel.application.VesselOwnershipMismatchException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.port.in.ValidateVesselAssignmentCommand;
import org.craft.mcs.cap.core.vessel.application.port.in.ValidateVesselAssignmentUseCase;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselRepository;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/** Validates material assignment against authoritative Vessel aggregate state. */
@Component
@RequiredArgsConstructor
public class ValidateVesselAssignmentService implements ValidateVesselAssignmentUseCase {

  private final @NonNull VesselRepository vesselRepository;
  private final @NonNull VesselCurrentContentQuery vesselCurrentContentQuery;
  private final @NonNull VesselAccessControl vesselAccessControl;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public @NonNull VesselReferenceView validate(@NonNull ValidateVesselAssignmentCommand command) {
    requireNonNull(command, "validate vessel assignment command cannot be null");
    vesselAccessControl.requireReadAccess(command.workspaceId());
    final var vessel = vesselRepository.lockVersionedById(command.vesselId())
        .orElseThrow(() -> new VesselNotFoundException(command.vesselId()));
    if (!vessel.aggregate().workspaceId().equals(command.workspaceId())) {
      throw new VesselOwnershipMismatchException(
          vessel.aggregate().vesselId(),
          command.workspaceId(),
          vessel.aggregate().workspaceId()
      );
    }
    final var currentContent = vesselCurrentContentQuery.findByVesselId(command.vesselId());
    if (currentContent.isPresent()) {
      throw new VesselOccupiedException(command.vesselId(), currentContent.orElseThrow());
    }
    vessel.aggregate().requireCanReceive(command.materialVolume());
    return VesselViewMapper.toReference(vessel.aggregate());
  }
}
