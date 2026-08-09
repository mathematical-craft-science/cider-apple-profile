package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.invitation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;

/** Persists workspace invitation aggregates independently from memberships. */
@Repository
@RequiredArgsConstructor
class JdbcWorkspaceInvitationRepository implements WorkspaceInvitationRepository {

  private final @NonNull SpringDataWorkspaceInvitationJdbcRepository repository;

  @Override
  public @NonNull WorkspaceInvitation create(@NonNull WorkspaceInvitation invitation) {
    requireNonNull(invitation, "workspace invitation cannot be null");
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace invitation",
        () -> repository.save(WorkspaceInvitationJdbcMapper.fromDomain(invitation))
    );
    return WorkspaceInvitationJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<WorkspaceInvitation> update(
      @NonNull VersionedAggregate<WorkspaceInvitation> invitation
  ) {
    requireNonNull(invitation, "versioned workspace invitation cannot be null");
    final var aggregate = invitation.aggregate();
    final var affectedRows = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace invitation",
        () -> repository.updateByIdAndVersion(
            aggregate.invitationId().value(),
            aggregate.status(),
            aggregate.acceptedByUserId() == null ? null : aggregate.acceptedByUserId().value(),
            aggregate.expiresAt(),
            aggregate.resendCount(),
            invitation.revision().value()
        )
    );
    return complete("workspace invitation", invitation, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<WorkspaceInvitation>> findVersionedById(
      @NonNull WorkspaceInvitationId invitationId
  ) {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    return repository.findById(invitationId.value())
        .map(WorkspaceInvitationJdbcMapper::toVersionedAggregate);
  }

  @Override
  public @NonNull Optional<@NonNull WorkspaceInvitation> findByWorkspaceIdAndInvitedEmailAndStatus(
      @NonNull WorkspaceId workspaceId,
      @NonNull EmailAddress invitedEmail,
      @NonNull WorkspaceInvitationStatus status
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(invitedEmail, "invited email cannot be null");
    requireNonNull(status, "workspace invitation status cannot be null");
    return repository.findByWorkspaceIdAndInvitedEmailAndStatus(
        workspaceReference(workspaceId),
        invitedEmail.value(),
        status
    ).map(WorkspaceInvitationJdbcMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull WorkspaceInvitation> findAllByWorkspaceId(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findAllByWorkspaceIdOrderByCreatedAtDescInvitationIdDesc(
        workspaceReference(workspaceId)
    ).stream().map(WorkspaceInvitationJdbcMapper::toDomain).toList();
  }

  @Override
  public @NonNull List<@NonNull WorkspaceInvitation> findAllByInvitedEmail(
      @NonNull EmailAddress invitedEmail
  ) {
    requireNonNull(invitedEmail, "invited email cannot be null");
    return repository.findAllByInvitedEmailOrderByCreatedAtDescInvitationIdDesc(
        invitedEmail.value()
    ).stream().map(WorkspaceInvitationJdbcMapper::toDomain).toList();
  }

  private static @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceReference(
      @NonNull WorkspaceId workspaceId
  ) {
    return AggregateReference.to(workspaceId.value());
  }
}
