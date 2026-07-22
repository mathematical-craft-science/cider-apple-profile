package org.craft.mcs.cap.infrastructure.adapter.out.delivery.account.invitation.smtp;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Rendered subject and alternative bodies of one workspace invitation email. */
record WorkspaceInvitationEmailContent(
    @NonNull String subject,
    @NonNull String plainText,
    @NonNull String htmlText
) {

  WorkspaceInvitationEmailContent {
    requireNonNull(subject, "workspace invitation email subject cannot be null");
    requireNonNull(plainText, "workspace invitation plain text cannot be null");
    requireNonNull(htmlText, "workspace invitation HTML cannot be null");
  }
}
