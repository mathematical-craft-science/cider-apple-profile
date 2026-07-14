package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.WorkspaceInvitationJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.WorkspaceJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;


/**
 * Spring Data command repository for WorkspaceInvitation JDBC entities.
 */
interface SpringDataWorkspaceInvitationJdbcRepository
    extends ListCrudRepository<WorkspaceInvitationJdbcEntity, @NonNull UUID> {

  @NonNull Optional<@NonNull WorkspaceInvitationJdbcEntity> findByWorkspaceIdAndInvitedEmailAndStatus(
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId,
      @NonNull String invitedEmail,
      @NonNull WorkspaceInvitationStatus status
  );

  @NonNull List<@NonNull WorkspaceInvitationJdbcEntity> findAllByWorkspaceIdOrderByCreatedAtDescInvitationIdDesc(
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId
  );
}
