package org.craft.mcs.cap.infrastructure.adapter.in.outbox.account.invitation;

import io.namastack.outbox.handler.OutboxFailureContext;
import io.namastack.outbox.handler.OutboxRecordMetadata;
import io.namastack.outbox.handler.OutboxTypedHandlerWithFallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.craft.mcs.cap.core.account.invitation.application.port.in.DeliverWorkspaceInvitationEmailUseCase;
import org.craft.mcs.cap.infrastructure.messaging.account.invitation.WorkspaceInvitationEmailOutboxPayload;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/** Dispatches persisted invitation delivery requests and records terminal failures without recipient PII. */
@Slf4j
@Component
@RequiredArgsConstructor
public final class WorkspaceInvitationEmailOutboxHandler
    implements OutboxTypedHandlerWithFallback<WorkspaceInvitationEmailOutboxPayload> {

  private final @NonNull DeliverWorkspaceInvitationEmailUseCase deliverWorkspaceInvitationEmailUseCase;

  @Override
  public void handle(
      @NonNull WorkspaceInvitationEmailOutboxPayload payload,
      @NonNull OutboxRecordMetadata metadata
  ) {
    deliverWorkspaceInvitationEmailUseCase.deliver(payload.toRequest());
  }

  @Override
  public void handleFailure(
      @NonNull WorkspaceInvitationEmailOutboxPayload payload,
      @NonNull OutboxFailureContext context
  ) {
    final var lastFailure = context.getLastFailure();
    final var failureType = lastFailure == null ? "unknown" : lastFailure.getClass().getName(); // NOSONAR
    log.error(
        "Workspace invitation email delivery failed permanently: invitationId={}, resendCount={}, failures={}, "
            + "failureType={}",
        payload.invitationId(),
        payload.resendCount(),
        context.getFailureCount(),
        failureType
    );
  }
}
