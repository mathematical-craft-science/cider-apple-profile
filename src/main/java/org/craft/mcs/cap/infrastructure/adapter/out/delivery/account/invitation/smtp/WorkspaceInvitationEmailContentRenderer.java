package org.craft.mcs.cap.infrastructure.adapter.out.delivery.account.invitation.smtp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationEmail;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.infrastructure.configuration.WorkspaceInvitationEmailProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Objects.requireNonNull;

/** Renders escaped invitation data into stable plain-text and HTML classpath templates. */
@Component
public final class WorkspaceInvitationEmailContentRenderer {

  private static final String TEMPLATE_ROOT = "mail/workspace-invitation/";
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([a-z_]+)}}");
  private static final DateTimeFormatter EXPIRATION_FORMAT = DateTimeFormatter
      .ofPattern("d MMM uuuu, HH:mm 'UTC'", Locale.ENGLISH)
      .withZone(ZoneOffset.UTC);

  private final @NonNull WorkspaceInvitationEmailProperties properties;
  private final @NonNull String plainTextTemplate;
  private final @NonNull String htmlTemplate;

  public WorkspaceInvitationEmailContentRenderer(
      @NonNull WorkspaceInvitationEmailProperties properties
  ) {
    this.properties = requireNonNull(properties, "workspace invitation email properties cannot be null");
    this.plainTextTemplate = readTemplate("workspace-invitation.txt");
    this.htmlTemplate = readTemplate("workspace-invitation.html");
  }

  @NonNull WorkspaceInvitationEmailContent render(@NonNull WorkspaceInvitationEmail email) {
    requireNonNull(email, "workspace invitation email cannot be null");
    final var invitationUrl = invitationUrl(email);
    final var expiresAt = EXPIRATION_FORMAT.format(email.expiresAt());
    final var role = roleLabel(email.role());
    final var plainValues = Map.of(
        "inviter_name", email.inviterName().value(),
        "workspace_name", email.workspaceName().value(),
        "role_name", role,
        "invited_email", email.recipient().value(),
        "expires_at", expiresAt,
        "invitation_url", invitationUrl
    );
    final var htmlValues = plainValues.entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey,
            entry -> HtmlUtils.htmlEscape(entry.getValue())
        ));

    return new WorkspaceInvitationEmailContent(
        subject(email.workspaceName().value()),
        render(plainTextTemplate, plainValues),
        render(htmlTemplate, htmlValues)
    );
  }

  private @NonNull String invitationUrl(@NonNull WorkspaceInvitationEmail email) {
    return UriComponentsBuilder.fromUri(properties.frontendBaseUrl())
        .pathSegment("account", "invitations", email.invitationId().value().toString())
        .build()
        .toUriString();
  }

  private static @NonNull String subject(@NonNull String workspaceName) {
    final var safeWorkspaceName = workspaceName
        .replace('\r', ' ')
        .replace('\n', ' ');
    return "You're invited to join \"%s\" in CAP".formatted(safeWorkspaceName);
  }

  private static @NonNull String render(
      @NonNull String template,
      @NonNull Map<@NonNull String, @NonNull String> values
  ) {
    final var matcher = PLACEHOLDER.matcher(template);
    final var rendered = new StringBuilder(template.length());
    while (matcher.find()) {
      final var placeholderName = matcher.group(1);
      final var value = values.get(placeholderName);
      if (value == null) {
        throw new WorkspaceInvitationEmailRenderingException(
            "unsupported workspace invitation placeholder: " + placeholderName
        );
      }
      matcher.appendReplacement(rendered, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(rendered);
    return rendered.toString();
  }

  private static @NonNull String readTemplate(@NonNull String name) {
    try {
      return new ClassPathResource(TEMPLATE_ROOT + name)
          .getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new WorkspaceInvitationEmailRenderingException(
          "cannot load workspace invitation email template: " + name,
          exception
      );
    }
  }

  private static @NonNull String roleLabel(@NonNull AccountRole role) {
    return switch (role) {
      case OWNER -> "Owner";
      case ADMIN -> "Administrator";
      case TECHNOLOGIST -> "Technologist";
      case CIDERMAKER -> "Cidermaker";
      case CATALOG_CONTRIBUTOR -> "Catalog contributor";
      case LAB_CONTRIBUTOR -> "Laboratory contributor";
      case CURATOR -> "Curator";
      case VIEWER -> "Viewer";
    };
  }
}
