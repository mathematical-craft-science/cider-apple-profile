package org.craft.mcs.cap.core.account.membership.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Aggregate repository port for workspace memberships. */
public interface WorkspaceMembershipRepository
    extends VersionedAggregateRepository<WorkspaceMembership, WorkspaceMembershipId> {

  /** Returns memberships that still represent current workspace participation, including suspended access. */
  @NonNull List<@NonNull WorkspaceMembership> findAllByWorkspaceId(@NonNull WorkspaceId workspaceId);

  @NonNull Optional<@NonNull VersionedAggregate<WorkspaceMembership>> findVersionedByWorkspaceIdAndUserId(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId userId
  );
}
