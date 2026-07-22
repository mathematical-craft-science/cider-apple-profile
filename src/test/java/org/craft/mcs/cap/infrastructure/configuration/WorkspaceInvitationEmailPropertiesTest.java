package org.craft.mcs.cap.infrastructure.configuration;

import java.net.URI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceInvitationEmailPropertiesTest {

  @Test
  void acceptsHttpFrontendAndNormalizesSenderText() {
    final var properties = new WorkspaceInvitationEmailProperties(
        URI.create("https://cap.example.com"),
        " no-reply@cap.example.com ",
        " CAP "
    );

    assertThat(properties)
        .returns(URI.create("https://cap.example.com"), WorkspaceInvitationEmailProperties::frontendBaseUrl)
        .returns("no-reply@cap.example.com", WorkspaceInvitationEmailProperties::senderAddress)
        .returns("CAP", WorkspaceInvitationEmailProperties::senderName);
  }

  @Test
  void rejectsNonHttpFrontendUrl() {
    assertThatThrownBy(() -> new WorkspaceInvitationEmailProperties(
        URI.create("file:/tmp/cap"),
        "no-reply@cap.example.com",
        "CAP"
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("frontend base URL must be an absolute HTTP or HTTPS URL");
  }

  @Test
  void rejectsInvalidSenderAddress() {
    assertThatThrownBy(() -> new WorkspaceInvitationEmailProperties(
        URI.create("https://cap.example.com"),
        "invalid",
        "CAP"
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invitation sender address has invalid format");
  }
}
