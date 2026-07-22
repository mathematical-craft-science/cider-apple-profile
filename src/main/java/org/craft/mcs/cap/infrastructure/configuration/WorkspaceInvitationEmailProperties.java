package org.craft.mcs.cap.infrastructure.configuration;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static java.util.Objects.requireNonNull;

/** External address and sender identity used by the workspace invitation email adapter. */
@ConfigurationProperties(prefix = "cap.account.invitation-email")
public record WorkspaceInvitationEmailProperties(
    /* Public CAP frontend root used to construct an invitation route. */
    @NonNull URI frontendBaseUrl,
    /* RFC-style sender address passed to the configured SMTP provider. */
    @NonNull String senderAddress,
    /* Human-readable sender name shown by email clients. */
    @NonNull String senderName
) {

  public WorkspaceInvitationEmailProperties(
      @DefaultValue("http://localhost:5173") @NonNull URI frontendBaseUrl,
      @DefaultValue("no-reply@cap.local") @NonNull String senderAddress,
      @DefaultValue("Cider Analytical Platform") @NonNull String senderName
  ) {
    this.frontendBaseUrl = requireNonNull(frontendBaseUrl, "frontend base URL cannot be null");
    this.senderAddress = requireEmailAddress(senderAddress);
    this.senderName = requireText(senderName, "invitation sender name");
    if (!isHttpUrl(this.frontendBaseUrl)) {
      throw new IllegalArgumentException("frontend base URL must be an absolute HTTP or HTTPS URL");
    }
  }

  private static boolean isHttpUrl(@NonNull URI value) {
    return value.isAbsolute()
        && value.getHost() != null
        && ("http".equalsIgnoreCase(value.getScheme()) || "https".equalsIgnoreCase(value.getScheme()));
  }

  private static @NonNull String requireEmailAddress(@NonNull String value) {
    final var normalized = requireText(value, "invitation sender address");
    try {
      return new InternetAddress(normalized, true).getAddress();
    } catch (AddressException exception) {
      throw new IllegalArgumentException("invitation sender address has invalid format", exception);
    }
  }

  private static @NonNull String requireText(
      @NonNull String value,
      @NonNull String fieldName
  ) {
    requireNonNull(value, fieldName + " cannot be null");
    final var normalized = value.strip();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
    return normalized;
  }
}
