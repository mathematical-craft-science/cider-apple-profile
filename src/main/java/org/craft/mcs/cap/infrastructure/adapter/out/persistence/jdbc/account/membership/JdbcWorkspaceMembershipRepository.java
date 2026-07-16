package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.membership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user.AccountUserJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcAggregateRevisionGuard.requireExpectedRevision;

/** Persists workspace membership aggregates and membership ownership references. */
@Repository
@RequiredArgsConstructor
class JdbcWorkspaceMembershipRepository implements WorkspaceMembershipRepository {

  private final @NonNull SpringDataWorkspaceMembershipJdbcRepository repository;

  @Override
  public @NonNull WorkspaceMembership create(@NonNull WorkspaceMembership membership) {
    requireNonNull(membership, "workspace membership cannot be null");
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace membership",
        () -> repository.save(WorkspaceMembershipJdbcMapper.fromDomain(membership))
    );
    return WorkspaceMembershipJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<WorkspaceMembership> update(
      @NonNull VersionedAggregate<WorkspaceMembership> membership
  ) {
    requireNonNull(membership, "versioned workspace membership cannot be null");
    final var aggregate = membership.aggregate();
    final var entity = repository.findById(aggregate.membershipId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("workspace membership", null));
    requireExpectedRevision(
        "workspace membership",
        membership.revision(),
        WorkspaceMembershipJdbcMapper.revision(entity)
    );
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace membership",
        () -> repository.save(WorkspaceMembershipJdbcMapper.updateFromDomain(aggregate, entity))
    );
    return WorkspaceMembershipJdbcMapper.toVersionedAggregate(saved);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<WorkspaceMembership>> findVersionedById(
      @NonNull WorkspaceMembershipId membershipId
  ) {
    requireNonNull(membershipId, "workspace membership id cannot be null");
    return repository.findById(membershipId.value()).map(WorkspaceMembershipJdbcMapper::toVersionedAggregate);
  }

  @Override
  public void delete(@NonNull VersionedAggregate<WorkspaceMembership> membership) {
    requireNonNull(membership, "versioned workspace membership cannot be null");
    final var affectedRows = repository.deleteByIdAndVersion(
        membership.aggregate().membershipId().value(),
        membership.revision().value()
    );
    if (affectedRows != 1) {
      throw new ConcurrentResourceModificationException("workspace membership", null);
    }
  }

  @Override
  public @NonNull List<@NonNull WorkspaceMembership> findAllByUserId(@NonNull AccountUserId userId) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findAllByUserIdOrderByCreatedAtAscMembershipIdAsc(userReference(userId))
        .stream()
        .map(WorkspaceMembershipJdbcMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull List<@NonNull WorkspaceMembership> findAllByWorkspaceId(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findAllByWorkspaceIdOrderByCreatedAtAscMembershipIdAsc(workspaceReference(workspaceId))
        .stream()
        .map(WorkspaceMembershipJdbcMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull Optional<@NonNull WorkspaceMembership> findByWorkspaceIdAndUserId(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId userId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(userId, "account user id cannot be null");
    return repository.findByWorkspaceIdAndUserId(workspaceReference(workspaceId), userReference(userId))
        .map(WorkspaceMembershipJdbcMapper::toDomain);
  }

  private static @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceReference(
      @NonNull WorkspaceId workspaceId
  ) {
    return AggregateReference.to(workspaceId.value());
  }

  private static @NonNull AggregateReference<AccountUserJdbcEntity, @NonNull UUID> userReference(
      @NonNull AccountUserId userId
  ) {
    return AggregateReference.to(userId.value());
  }
}
