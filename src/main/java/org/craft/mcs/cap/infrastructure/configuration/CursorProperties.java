package org.craft.mcs.cap.infrastructure.configuration;

import java.time.Duration;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorCodecConfigurationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static java.util.Objects.requireNonNull;


/** External settings for versioned HMAC-signed database pagination cursors. */
@ConfigurationProperties(prefix = "cap.query.cursor")
public record CursorProperties(
    /* Validity period of an issued cursor token. */
    @NonNull Duration timeToLive,
    /* Key used to sign newly issued cursor tokens. */
    @NonNull SigningKeyProperties activeKey,
    /* Previous key accepted during a controlled signing-key rotation. */
    @Nullable SigningKeyProperties previousKey
) {

  public CursorProperties {
    requireNonNull(timeToLive, "cursor time to live cannot be null");
    requireNonNull(activeKey, "active cursor signing key cannot be null");

    if (timeToLive.isZero() || timeToLive.isNegative()) {
      throw new CursorCodecConfigurationException("cursor time to live must be positive");
    }
    if (previousKey != null && activeKey.id().equals(previousKey.id())) {
      throw new CursorCodecConfigurationException(
          "active and previous cursor signing keys must have different ids"
      );
    }
  }

  /** One Base64-encoded HMAC signing key and its stable rotation identifier. */
  public record SigningKeyProperties(
      /* Identifier embedded in cursor tokens to select the verification key. */
      @NonNull String id,
      /* Base64-encoded HMAC secret containing at least 256 bits of entropy. */
      @NonNull String secret
  ) {

    public SigningKeyProperties {
      requireNonNull(id, "cursor signing key id cannot be null");
      requireNonNull(secret, "cursor signing key secret cannot be null");
      id = id.strip();
      secret = secret.strip();

      if (id.isEmpty() || !id.matches("[A-Za-z0-9_-]{1,40}")) {
        throw new CursorCodecConfigurationException("cursor signing key id is invalid");
      }
      if (secret.isEmpty()) {
        throw new CursorCodecConfigurationException("cursor signing key secret cannot be blank");
      }
    }
  }
}
