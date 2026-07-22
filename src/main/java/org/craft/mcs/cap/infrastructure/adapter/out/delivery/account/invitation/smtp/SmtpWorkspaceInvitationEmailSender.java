package org.craft.mcs.cap.infrastructure.adapter.out.delivery.account.invitation.smtp;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationEmail;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationEmailSender;
import org.craft.mcs.cap.infrastructure.configuration.WorkspaceInvitationEmailProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED;

/** Sends multipart workspace invitation email with plain-text fallback and an inline CAP logo. */
@Component
@RequiredArgsConstructor
public final class SmtpWorkspaceInvitationEmailSender implements WorkspaceInvitationEmailSender {

  private static final String LOGO_CONTENT_ID = "cap-logo";
  private static final ClassPathResource LOGO_RESOURCE =
      new ClassPathResource("mail/workspace-invitation/cap-logo.png");

  private final @NonNull JavaMailSender mailSender;
  private final @NonNull WorkspaceInvitationEmailProperties properties;
  private final @NonNull WorkspaceInvitationEmailContentRenderer contentRenderer;

  @Override
  public void send(@NonNull WorkspaceInvitationEmail email) {
    requireNonNull(email, "workspace invitation email cannot be null");
    final var content = contentRenderer.render(email);
    mailSender.send(mimeMessage -> {
      final var helper = new MimeMessageHelper(
          mimeMessage,
          MULTIPART_MODE_MIXED_RELATED,
          StandardCharsets.UTF_8.name()
      );
      helper.setFrom(properties.senderAddress(), properties.senderName());
      helper.setTo(email.recipient().value());
      helper.setSubject(content.subject());
      helper.setText(content.plainText(), content.htmlText());
      helper.addInline(LOGO_CONTENT_ID, LOGO_RESOURCE, MediaType.IMAGE_PNG_VALUE);
    });
  }
}
