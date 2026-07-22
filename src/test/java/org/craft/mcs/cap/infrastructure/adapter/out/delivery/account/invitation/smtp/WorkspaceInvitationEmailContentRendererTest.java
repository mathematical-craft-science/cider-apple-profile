package org.craft.mcs.cap.infrastructure.adapter.out.delivery.account.invitation.smtp;

import java.net.URI;
import java.time.Instant;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationEmail;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.infrastructure.configuration.WorkspaceInvitationEmailProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceInvitationEmailContentRendererTest {

  @Test
  void rendersEscapedHtmlAndReadablePlainText() {
    final var properties = new WorkspaceInvitationEmailProperties(
        URI.create("https://cap.example.com"),
        "no-reply@cap.example.com",
        "CAP"
    );
    final var invitationId = WorkspaceInvitationId.newId();
    final var email = new WorkspaceInvitationEmail(
        invitationId,
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("Ezh & Voron"),
        AccountRole.TECHNOLOGIST,
        DisplayName.of("Paul <Owner>"),
        Instant.parse("2026-07-28T16:16:54.835797Z")
    );

    final var content = new WorkspaceInvitationEmailContentRenderer(properties).render(email);

    assertThat(content)
        .returns("You're invited to join \"Ezh & Voron\" in CAP", WorkspaceInvitationEmailContent::subject)
        .satisfies(actual -> assertThat(actual.plainText())
            .contains("Paul <Owner> invited you to join the CAP workspace \"Ezh & Voron\" as Technologist.")
            .contains("Invitation expires: 28 Jul 2026, 16:16 UTC")
            .contains("https://cap.example.com/account/invitations/" + invitationId.value()))
        .satisfies(actual -> assertThat(actual.htmlText())
            .contains("Paul &lt;Owner&gt;")
            .contains("Ezh &amp; Voron")
            .contains("28 Jul 2026, 16:16 UTC")
            .contains("cid:cap-logo")
            .contains("<table aria-label=\"Invitation details\"")
            .contains("<th scope=\"row\"")
            .contains("https://cap.example.com/account/invitations/" + invitationId.value())
            .doesNotContain(
                " align=\"",
                " valign=\"",
                " bgcolor=\"",
                " cellpadding=\"",
                " cellspacing=\"",
                " border=\""
            )
            .doesNotContain("{{"));
  }
}
