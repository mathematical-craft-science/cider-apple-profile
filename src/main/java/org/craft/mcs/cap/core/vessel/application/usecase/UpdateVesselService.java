package org.craft.mcs.cap.core.vessel.application.usecase;

import static java.util.Objects.requireNonNull;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.VesselNameConflictException;
import org.craft.mcs.cap.core.vessel.application.VesselNotFoundException;
import org.craft.mcs.cap.core.vessel.application.VesselOccupiedException;
import org.craft.mcs.cap.core.vessel.application.VesselOwnershipMismatchException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.port.in.UpdateVesselCommand;
import org.craft.mcs.cap.core.vessel.application.port.in.UpdateVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselRepository;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/**
 * Revision-checked application service for editing a Vessel aggregate.
 */
@Component
@RequiredArgsConstructor
public final class UpdateVesselService
    implements UpdateVesselUseCase {

  private final @NonNull VesselRepository vesselRepository;
  private final @NonNull VesselCurrentContentQuery vesselCurrentContentQuery;
  private final @NonNull VesselAccessControl vesselAccessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull VesselUsageView update(@NonNull UpdateVesselCommand command) {
    requireNonNull(command, "update vessel command cannot be null");
    vesselAccessControl.requireWriteAccess(command.workspaceId());
    return transactionHelper.callInTransactionNonNull(() -> updateInTransaction(command));
  }

  private @NonNull VesselUsageView updateInTransaction(@NonNull UpdateVesselCommand command) {
    final var loaded = vesselRepository.lockVersionedById(command.vesselId())
        .orElseThrow(() -> new VesselNotFoundException(command.vesselId()));
    final var vessel = loaded.aggregate();
    requireOwnedByWorkspace(command, vessel.workspaceId());
    if (!loaded.revision().equals(command.expectedRevision())) {
      throw new ConcurrentResourceModificationException("vessel", null);
    }
    requireUniqueName(command);
    final var currentContent = vesselCurrentContentQuery.findByVesselId(vessel.vesselId())
        .orElse(null); // NOSONAR
    if (currentContent != null && command.operationalStatus() != VesselOperationalStatus.OPERATIONAL) {
      throw new VesselOccupiedException(vessel.vesselId(), currentContent);
    }
    vessel.updateCharacteristics(command.name(), command.nominalCapacity(), command.material())
        .changeOperationalStatus(command.operationalStatus());
    if (currentContent != null) {
      vessel.requireCapacityFor(currentContent.volume());
    }
    return new VesselUsageView(
        VesselViewMapper.toView(vesselRepository.update(loaded.withAggregate(vessel))),
        currentContent
    );
  }

  private static void requireOwnedByWorkspace(
      @NonNull UpdateVesselCommand command,
      @NonNull WorkspaceId actualWorkspaceId
  ) {
    if (!actualWorkspaceId.equals(command.workspaceId())) {
      throw new VesselOwnershipMismatchException(
          command.vesselId(),
          command.workspaceId(),
          actualWorkspaceId
      );
    }
  }

  private void requireUniqueName(@NonNull UpdateVesselCommand command) {
    if (vesselRepository.existsByWorkspaceAndNameExcluding(
        command.workspaceId(),
        command.name(),
        command.vesselId()
    )) {
      throw new VesselNameConflictException(command.workspaceId(), command.name());
    }
  }
}
