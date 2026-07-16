package org.craft.mcs.cap.core.account.workspace.application.port.out;

import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;

/** Aggregate repository port for CAP workspaces. */
public interface WorkspaceRepository
    extends VersionedAggregateRepository<Workspace, WorkspaceId> {
}
