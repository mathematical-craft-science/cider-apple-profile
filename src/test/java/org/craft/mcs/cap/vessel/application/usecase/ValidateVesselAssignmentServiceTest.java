package org.craft.mcs.cap.vessel.application.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.vessel.application.VesselOccupiedException;
import org.craft.mcs.cap.core.vessel.application.VesselOwnershipMismatchException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentType;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentView;
import org.craft.mcs.cap.core.vessel.application.port.in.ValidateVesselAssignmentCommand;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselRepository;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.craft.mcs.cap.core.vessel.application.usecase.ValidateVesselAssignmentService;
import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class ValidateVesselAssignmentServiceTest {

  @Test
  void returnsReferenceForAssignableWorkspaceVessel() {
    final var repository = mock(VesselRepository.class);
    final var accessControl = mock(VesselAccessControl.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var vessel = vessel(WorkspaceId.newId());
    when(repository.lockVersionedById(vessel.vesselId())).thenReturn(Optional.of(versioned(vessel)));
    when(currentContentQuery.findByVesselId(vessel.vesselId())).thenReturn(Optional.empty());
    final var service = new ValidateVesselAssignmentService(repository, currentContentQuery, accessControl);

    final var reference = service.validate(new ValidateVesselAssignmentCommand(
        vessel.vesselId(),
        vessel.workspaceId(),
        VolumeValue.ofLiters("500")
    ));

    assertThat(reference)
        .returns(vessel.vesselId(), VesselReferenceView::vesselId)
        .returns("Tank F-03", VesselReferenceView::name);
    verify(accessControl).requireReadAccess(vessel.workspaceId());
  }

  @Test
  void rejectsCrossWorkspaceAssignment() {
    final var repository = mock(VesselRepository.class);
    final var vessel = vessel(WorkspaceId.newId());
    final var requestedWorkspaceId = WorkspaceId.newId();
    when(repository.lockVersionedById(vessel.vesselId())).thenReturn(Optional.of(versioned(vessel)));
    final var service = new ValidateVesselAssignmentService(
        repository,
        mock(VesselCurrentContentQuery.class),
        mock(VesselAccessControl.class)
    );

    assertThatThrownBy(() -> service.validate(new ValidateVesselAssignmentCommand(
        vessel.vesselId(),
        requestedWorkspaceId,
        VolumeValue.ofLiters("100")
    )))
        .isInstanceOf(VesselOwnershipMismatchException.class);
  }

  @Test
  void rejectsAssignmentAboveNominalCapacity() {
    final var repository = mock(VesselRepository.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var vessel = vessel(WorkspaceId.newId());
    when(repository.lockVersionedById(vessel.vesselId())).thenReturn(Optional.of(versioned(vessel)));
    when(currentContentQuery.findByVesselId(vessel.vesselId())).thenReturn(Optional.empty());
    final var service = new ValidateVesselAssignmentService(
        repository,
        currentContentQuery,
        mock(VesselAccessControl.class)
    );

    assertThatThrownBy(() -> service.validate(new ValidateVesselAssignmentCommand(
        vessel.vesselId(),
        vessel.workspaceId(),
        VolumeValue.ofLiters("500.001")
    )))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("material volume exceeds vessel nominal capacity");
  }

  @Test
  void rejectsAssignmentWhenVesselAlreadyContainsActiveMaterial() {
    final var repository = mock(VesselRepository.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var vessel = vessel(WorkspaceId.newId());
    final var currentContent = new VesselCurrentContentView(
        vessel.vesselId(),
        VesselCurrentContentType.FEEDSTOCK_BATCH,
        newUuid(),
        MaterialStateId.newId(),
        BatchName.of("Existing juice"),
        VolumeValue.ofLiters("100")
    );
    when(repository.lockVersionedById(vessel.vesselId())).thenReturn(Optional.of(versioned(vessel)));
    when(currentContentQuery.findByVesselId(vessel.vesselId())).thenReturn(Optional.of(currentContent));
    final var service = new ValidateVesselAssignmentService(
        repository,
        currentContentQuery,
        mock(VesselAccessControl.class)
    );

    assertThatThrownBy(() -> service.validate(new ValidateVesselAssignmentCommand(
        vessel.vesselId(),
        vessel.workspaceId(),
        VolumeValue.ofLiters("50")
    )))
        .isInstanceOf(VesselOccupiedException.class)
        .hasMessageContaining("is occupied by FEEDSTOCK_BATCH");
  }

  private static VersionedAggregate<Vessel> versioned(Vessel vessel) {
    return new VersionedAggregate<>(vessel, AggregateRevision.of(1));
  }

  private static Vessel vessel(WorkspaceId workspaceId) {
    return Vessel.create(
        VesselId.newId(),
        workspaceId,
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL
    );
  }
}
