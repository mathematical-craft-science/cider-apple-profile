package org.craft.mcs.cap.core.vessel.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.VesselNameConflictException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.port.in.CreateVesselCommand;
import org.craft.mcs.cap.core.vessel.application.port.in.CreateVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselRepository;
import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Registers one workspace-owned Vessel aggregate after enforcing catalog name uniqueness. */
@Component
@RequiredArgsConstructor
public final class CreateVesselService implements CreateVesselUseCase {

  private final @NonNull VesselRepository vesselRepository;
  private final @NonNull VesselAccessControl vesselAccessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull VesselUsageView create(@NonNull CreateVesselCommand command) {
    requireNonNull(command, "create vessel command cannot be null");
    vesselAccessControl.requireWriteAccess(command.workspaceId());
    return transactionHelper.callInTransactionNonNull(() -> createInTransaction(command));
  }

  private @NonNull VesselUsageView createInTransaction(@NonNull CreateVesselCommand command) {
    final var vesselId = VesselId.newId();
    requireUniqueName(command, vesselId);
    final var saved = vesselRepository.create(Vessel.create(
        vesselId,
        command.workspaceId(),
        command.name(),
        command.nominalCapacity(),
        command.material()
    ));
    return VesselUsageView.empty(VesselViewMapper.toView(saved));
  }

  private void requireUniqueName(
      @NonNull CreateVesselCommand command,
      @NonNull VesselId vesselId
  ) {
    if (vesselRepository.existsByWorkspaceAndNameExcluding(
        command.workspaceId(),
        command.name(),
        vesselId
    )) {
      throw new VesselNameConflictException(command.workspaceId(), command.name());
    }
  }
}
