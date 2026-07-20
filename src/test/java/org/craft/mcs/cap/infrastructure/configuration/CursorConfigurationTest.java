package org.craft.mcs.cap.infrastructure.configuration;

import java.time.Clock;
import java.time.Duration;
import org.craft.mcs.cap.infrastructure.configuration.CursorProperties.SigningKeyProperties;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorCodecConfigurationException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CursorConfigurationTest {

  @Test
  void rejectsNonPositiveCursorLifetimeWithConfigurationException() {
    final var activeKey = new SigningKeyProperties("active", "configured-secret");

    assertThatThrownBy(() -> new CursorProperties(Duration.ZERO, activeKey, null))
        .isInstanceOf(CursorCodecConfigurationException.class)
        .hasMessage("cursor time to live must be positive");
  }

  @Test
  void rejectsInvalidBase64SecretWithConfigurationException() {
    final var properties = new CursorProperties(
        Duration.ofHours(1),
        new SigningKeyProperties("active", "***"),
        null
    );

    assertThatThrownBy(() -> new CursorConfiguration().cursorTokenCodec(
        JsonMapper.builder().findAndAddModules().build(),
        Clock.systemUTC(),
        properties
    ))
        .isInstanceOf(CursorCodecConfigurationException.class)
        .hasMessage("cursor signing key secret must be valid Base64");
  }
}
