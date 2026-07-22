package org.craft.mcs.cap.infrastructure.adapter.out.delivery.account.invitation.outbox;

import io.namastack.outbox.Outbox;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDeliveryRequest;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationDeliveryScheduler;
import org.craft.mcs.cap.infrastructure.messaging.account.invitation.WorkspaceInvitationEmailOutboxPayload;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

/** Persists invitation delivery requests through Namastack's transactional JDBC outbox. */
@Component
@RequiredArgsConstructor
public class NamastackWorkspaceInvitationDeliveryScheduler
    implements WorkspaceInvitationDeliveryScheduler {

  private final @NonNull Outbox outbox;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void schedule(@NonNull WorkspaceInvitationDeliveryRequest request) {
    requireNonNull(request, "workspace invitation delivery request cannot be null");
    outbox.schedule(
        WorkspaceInvitationEmailOutboxPayload.from(request),
        request.invitationId().value().toString()
    );
  }
}
