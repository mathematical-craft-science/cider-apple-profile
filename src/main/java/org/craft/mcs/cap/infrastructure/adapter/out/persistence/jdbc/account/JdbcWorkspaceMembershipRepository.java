package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountUserJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.WorkspaceJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;

/** Persists workspace membership aggregates and membership ownership references. */
@Repository
@RequiredArgsConstructor
class JdbcWorkspaceMembershipRepository implements WorkspaceMembershipRepository {

  private final @NonNull SpringDataWorkspaceMembershipJdbcRepository repository;

  @Override
  public @NonNull WorkspaceMembership save(@NonNull WorkspaceMembership membership) {
    requireNonNull(membership, "workspace membership cannot be null");
    final var entity = repository.findById(membership.membershipId().value())
        .map(existing -> AccountJdbcMapper.updateFromDomain(membership, existing))
        .orElseGet(() -> AccountJdbcMapper.fromDomain(membership));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace membership",
        () -> repository.save(entity)
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public void deleteById(@NonNull WorkspaceMembershipId membershipId) {
    requireNonNull(membershipId, "workspace membership id cannot be null");
    repository.deleteById(membershipId.value());
  }

  @Override
  public @NonNull Optional<@NonNull WorkspaceMembership> findById(
      @NonNull WorkspaceMembershipId membershipId
  ) {
    requireNonNull(membershipId, "workspace membership id cannot be null");
    return repository.findById(membershipId.value()).map(AccountJdbcMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull WorkspaceMembership> findAllByUserId(@NonNull AccountUserId userId) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findAllByUserIdOrderByCreatedAtAscMembershipIdAsc(userReference(userId))
        .stream()
        .map(AccountJdbcMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull List<@NonNull WorkspaceMembership> findAllByWorkspaceId(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findAllByWorkspaceIdOrderByCreatedAtAscMembershipIdAsc(workspaceReference(workspaceId))
        .stream()
        .map(AccountJdbcMapper::toDomain)
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
        .map(AccountJdbcMapper::toDomain);
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
