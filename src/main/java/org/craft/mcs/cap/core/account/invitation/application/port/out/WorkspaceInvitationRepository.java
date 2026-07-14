package org.craft.mcs.cap.core.account.invitation.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Aggregate repository port for workspace invitations that precede membership creation. */
public interface WorkspaceInvitationRepository {

  @NonNull WorkspaceInvitation save(@NonNull WorkspaceInvitation invitation);

  @NonNull Optional<@NonNull WorkspaceInvitation> findById(
      @NonNull WorkspaceInvitationId invitationId
  );

  @NonNull Optional<@NonNull WorkspaceInvitation> findByWorkspaceIdAndInvitedEmailAndStatus(
      @NonNull WorkspaceId workspaceId,
      @NonNull EmailAddress invitedEmail,
      @NonNull WorkspaceInvitationStatus status
  );

  @NonNull List<@NonNull WorkspaceInvitation> findAllByWorkspaceId(
      @NonNull WorkspaceId workspaceId
  );
}
