package org.craft.mcs.cap.core.account.membership.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Aggregate repository port for workspace memberships. */
public interface WorkspaceMembershipRepository {

  @NonNull WorkspaceMembership save(@NonNull WorkspaceMembership membership);

  void deleteById(@NonNull WorkspaceMembershipId membershipId);

  @NonNull Optional<@NonNull WorkspaceMembership> findById(
      @NonNull WorkspaceMembershipId membershipId
  );

  @NonNull List<@NonNull WorkspaceMembership> findAllByUserId(@NonNull AccountUserId userId);

  @NonNull List<@NonNull WorkspaceMembership> findAllByWorkspaceId(@NonNull WorkspaceId workspaceId);

  @NonNull Optional<@NonNull WorkspaceMembership> findByWorkspaceIdAndUserId(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId userId
  );
}
