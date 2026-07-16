package org.craft.mcs.cap.core.account.invitation.domain.model;

import java.time.Duration;
import java.time.Instant;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Invitation aggregate that precedes and is independent from a registered workspace membership. */
@AggregateRoot
public record WorkspaceInvitation(
    @NonNull WorkspaceInvitationId invitationId,
    @NonNull WorkspaceId workspaceId,
    @NonNull EmailAddress invitedEmail,
    @NonNull AccountRole role,
    @NonNull WorkspaceInvitationStatus status,
    @NonNull AccountUserId invitedByUserId,
    @Nullable AccountUserId acceptedByUserId,
    @NonNull Instant expiresAt,
    int resendCount
) {

  public WorkspaceInvitation {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(invitedEmail, "invited email cannot be null");
    requireNonNull(role, "invited workspace role cannot be null");
    requireNonNull(status, "workspace invitation status cannot be null");
    requireNonNull(invitedByUserId, "inviting account user id cannot be null");
    requireNonNull(expiresAt, "workspace invitation expiration cannot be null");
    if (role == AccountRole.OWNER) {
      throw new WorkspaceInvitationRuleException("OWNER role can be assigned only by ownership transfer");
    }
    if (status == WorkspaceInvitationStatus.ACCEPTED && acceptedByUserId == null) {
      throw new WorkspaceInvitationRuleException("accepted invitation must identify the accepting account");
    }
    if (status != WorkspaceInvitationStatus.ACCEPTED && acceptedByUserId != null) {
      throw new WorkspaceInvitationRuleException(
          "only an accepted invitation can identify an accepting account"
      );
    }
    if (resendCount < 0) {
      throw new WorkspaceInvitationRuleException("workspace invitation resend count cannot be negative");
    }
  }

  /** Creates a pending invitation for a normalized recipient email. */
  public static @NonNull WorkspaceInvitation create(
      @NonNull WorkspaceId workspaceId,
      @NonNull EmailAddress invitedEmail,
      @NonNull AccountRole role,
      @NonNull AccountUserId invitedByUserId,
      @NonNull Instant now,
      @NonNull Duration timeToLive
  ) {
    requirePositive(timeToLive);
    return new WorkspaceInvitation(
        WorkspaceInvitationId.newId(),
        workspaceId,
        invitedEmail,
        role,
        WorkspaceInvitationStatus.PENDING,
        invitedByUserId,
        null,
        now.plus(timeToLive),
        0
    );
  }

  /** Returns the status visible at the given time without mutating persisted history. */
  public @NonNull WorkspaceInvitationStatus effectiveStatus(@NonNull Instant now) {
    requireNonNull(now, "current time cannot be null");
    return status == WorkspaceInvitationStatus.PENDING && !now.isBefore(expiresAt)
        ? WorkspaceInvitationStatus.EXPIRED
        : status;
  }

  /** Accepts the invitation after the application has verified the authenticated recipient email. */
  public @NonNull WorkspaceInvitation accept(
      @NonNull AccountUserId acceptingUserId,
      @NonNull Instant now
  ) {
    requirePending(now);
    return with(WorkspaceInvitationStatus.ACCEPTED, acceptingUserId, expiresAt, resendCount);
  }

  /** Records an explicit rejection by the intended recipient. */
  public @NonNull WorkspaceInvitation reject(@NonNull Instant now) {
    requirePending(now);
    return with(WorkspaceInvitationStatus.REJECTED, null, expiresAt, resendCount);
  }

  /** Revokes a pending invitation on behalf of a workspace administrator. */
  public @NonNull WorkspaceInvitation revoke(@NonNull Instant now) {
    requirePending(now);
    return with(WorkspaceInvitationStatus.REVOKED, null, expiresAt, resendCount);
  }

  /** Persists the elapsed state when a due invitation is processed. */
  public @NonNull WorkspaceInvitation expire(@NonNull Instant now) {
    if (effectiveStatus(now) != WorkspaceInvitationStatus.EXPIRED) {
      throw new WorkspaceInvitationRuleException("only an elapsed pending invitation can expire");
    }
    return with(WorkspaceInvitationStatus.EXPIRED, null, expiresAt, resendCount);
  }

  /** Renews a pending or expired invitation for another delivery attempt. */
  public @NonNull WorkspaceInvitation resend(
      @NonNull Instant now,
      @NonNull Duration timeToLive
  ) {
    requirePositive(timeToLive);
    final var currentStatus = effectiveStatus(now);
    if (currentStatus != WorkspaceInvitationStatus.PENDING
        && currentStatus != WorkspaceInvitationStatus.EXPIRED) {
      throw new WorkspaceInvitationRuleException("only a pending or expired invitation can be resent");
    }
    return with(WorkspaceInvitationStatus.PENDING, null, now.plus(timeToLive), resendCount + 1);
  }

  public boolean isAddressedTo(@NonNull EmailAddress email) {
    requireNonNull(email, "account email cannot be null");
    return invitedEmail.value().equalsIgnoreCase(email.value());
  }

  private void requirePending(@NonNull Instant now) {
    if (effectiveStatus(now) != WorkspaceInvitationStatus.PENDING) {
      throw new WorkspaceInvitationRuleException("workspace invitation is not pending");
    }
  }

  private @NonNull WorkspaceInvitation with(
      @NonNull WorkspaceInvitationStatus updatedStatus,
      @Nullable AccountUserId acceptingUserId,
      @NonNull Instant updatedExpiresAt,
      int updatedResendCount
  ) {
    return new WorkspaceInvitation(
        invitationId,
        workspaceId,
        invitedEmail,
        role,
        updatedStatus,
        invitedByUserId,
        acceptingUserId,
        updatedExpiresAt,
        updatedResendCount
    );
  }

  private static void requirePositive(@NonNull Duration timeToLive) {
    requireNonNull(timeToLive, "workspace invitation time to live cannot be null");
    if (timeToLive.isZero() || timeToLive.isNegative()) {
      throw new WorkspaceInvitationRuleException("workspace invitation time to live must be positive");
    }
  }
}
