package org.craft.mcs.cap.infrastructure.adapter.out.delivery.account.invitation.smtp;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationEmail;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.infrastructure.configuration.WorkspaceInvitationEmailProperties;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpWorkspaceInvitationEmailSenderTest {

  @Test
  void preparesWorkspaceSpecificInvitationEmail() throws Exception {
    final var mailSender = mock(JavaMailSender.class);
    final var properties = new WorkspaceInvitationEmailProperties(
        URI.create("https://cap.example.com"),
        "no-reply@cap.example.com",
        "CAP"
    );
    final var sender = new SmtpWorkspaceInvitationEmailSender(
        mailSender,
        properties,
        new WorkspaceInvitationEmailContentRenderer(properties)
    );
    final var invitationId = WorkspaceInvitationId.newId();
    final var email = new WorkspaceInvitationEmail(
        invitationId,
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("Summer Cidery"),
        AccountRole.TECHNOLOGIST,
        DisplayName.of("Workspace Owner"),
        Instant.parse("2026-07-21T12:00:00Z")
    );

    sender.send(email);

    final var preparatorCaptor = ArgumentCaptor.forClass(MimeMessagePreparator.class);
    verify(mailSender).send(preparatorCaptor.capture());
    final var message = new MimeMessage(Session.getInstance(new Properties()));
    preparatorCaptor.getValue().prepare(message);
    message.saveChanges();

    assertThat(snapshot(message))
        .returns("You're invited to join \"Summer Cidery\" in CAP", SentMessageSnapshot::subject)
        .returns(List.of("technologist@example.com"), SentMessageSnapshot::recipients)
        .returns(List.of("CAP <no-reply@cap.example.com>"), SentMessageSnapshot::senders)
        .satisfies(snapshot -> assertThat(snapshot.contentType())
            .contains("multipart/mixed"))
        .satisfies(snapshot -> assertThat(snapshot.rawMessage())
            .contains("Content-Type: text/plain")
            .contains("Content-Type: text/html")
            .contains("Content-Type: image/png")
            .contains("Content-ID: <cap-logo>"));
  }

  private static @NonNull SentMessageSnapshot snapshot(@NonNull MimeMessage message) throws Exception {
    final var output = new ByteArrayOutputStream();
    message.writeTo(output);
    return new SentMessageSnapshot(
        message.getSubject(),
        Arrays.stream(requireNonNull(message.getAllRecipients())).map(Object::toString).toList(),
        Arrays.stream(requireNonNull(message.getFrom())).map(Object::toString).toList(),
        message.getContentType(),
        output.toString(StandardCharsets.ISO_8859_1)
    );
  }

  private record SentMessageSnapshot(
      @NonNull String subject,
      @NonNull List<@NonNull String> recipients,
      @NonNull List<@NonNull String> senders,
      @NonNull String contentType,
      @NonNull String rawMessage
  ) {
  }
}
