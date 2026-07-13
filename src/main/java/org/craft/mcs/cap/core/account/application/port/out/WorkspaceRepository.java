package org.craft.mcs.cap.core.account.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;


/** Aggregate repository port for CAP workspaces. */
public interface WorkspaceRepository {

  @NonNull Workspace save(@NonNull Workspace workspace);

  @NonNull Optional<@NonNull Workspace> findById(@NonNull WorkspaceId workspaceId);

  @NonNull Optional<@NonNull Workspace> findByCiderMakerId(@NonNull CiderMakerId ciderMakerId);
}
