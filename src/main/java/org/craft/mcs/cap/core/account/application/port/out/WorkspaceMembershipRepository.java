package org.craft.mcs.cap.core.account.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Aggregate repository port for workspace memberships. */
public interface WorkspaceMembershipRepository {

  @NonNull WorkspaceMembership save(@NonNull WorkspaceMembership membership);

  @NonNull List<@NonNull WorkspaceMembership> findAllByUserId(@NonNull AccountUserId userId);

  @NonNull List<@NonNull WorkspaceMembership> findAllByWorkspaceId(@NonNull WorkspaceId workspaceId);

  @NonNull Optional<@NonNull WorkspaceMembership> findByWorkspaceIdAndUserId(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId userId
  );
}
