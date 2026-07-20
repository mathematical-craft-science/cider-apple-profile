package org.craft.mcs.cap.infrastructure.configuration;

import java.time.Clock;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.craft.mcs.cap.infrastructure.configuration.CursorProperties.SigningKeyProperties;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorCodecConfigurationException;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorTokenCodec;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.HmacCursorTokenCodec;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;


/** Wires the provider-neutral cursor contract to the configured HMAC implementation and signing keys. */
@Configuration
@EnableConfigurationProperties(CursorProperties.class)
class CursorConfiguration {

  private static final int MINIMUM_SECRET_BYTES = 32;

  @Bean
  @NonNull CursorTokenCodec cursorTokenCodec(
      @NonNull ObjectMapper objectMapper,
      @NonNull Clock clock,
      @NonNull CursorProperties properties
  ) {
    final var signingKeys = new HashMap<String, byte[]>();
    addSigningKey(signingKeys, properties.activeKey());

    final var previousKey = properties.previousKey();
    if (previousKey != null) {
      addSigningKey(signingKeys, previousKey);
    }

    return new HmacCursorTokenCodec(
        objectMapper,
        clock,
        properties.timeToLive(),
        properties.activeKey().id(),
        signingKeys
    );
  }

  private static void addSigningKey(
      @NonNull Map<@NonNull String, byte @NonNull []> signingKeys,
      @NonNull SigningKeyProperties properties
  ) {
    final byte[] decodedSecret;
    try {
      decodedSecret = Base64.getDecoder().decode(properties.secret());
    } catch (IllegalArgumentException exception) {
      throw new CursorCodecConfigurationException(
          "cursor signing key secret must be valid Base64",
          exception
      );
    }

    if (decodedSecret.length < MINIMUM_SECRET_BYTES) {
      throw new CursorCodecConfigurationException(
          "cursor signing key secret must contain at least 32 bytes"
      );
    }
    signingKeys.put(properties.id(), decodedSecret);
  }
}
