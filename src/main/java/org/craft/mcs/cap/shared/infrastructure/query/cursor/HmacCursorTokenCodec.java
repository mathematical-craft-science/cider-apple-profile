package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.craft.mcs.cap.shared.application.query.MalformedCursorException;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;


/**
 * Versioned compact cursor codec that signs the exact serialized payload with HMAC-SHA256.
 *
 * <p>The token is integrity-protected but not encrypted. Query scope and fingerprint prevent a
 * valid cursor from being replayed against a different endpoint, workspace, filter set, or sort.
 */
public final class HmacCursorTokenCodec implements CursorTokenCodec {

  private static final @NonNull String VERSION = "v1";
  private static final @NonNull String HMAC_ALGORITHM = "HmacSHA256";
  private static final @NonNull String FINGERPRINT_ALGORITHM = "SHA-256";
  private static final int MAX_TOKEN_LENGTH = 8_192;
  private static final int MINIMUM_KEY_BYTES = 32;
  private static final @NonNull Pattern VALID_KEY_ID = Pattern.compile("[A-Za-z0-9_-]{1,40}");
  private static final Base64.@NonNull Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.@NonNull Decoder DECODER = Base64.getUrlDecoder();

  private final @NonNull ObjectMapper objectMapper;
  private final @NonNull Clock clock;
  private final @NonNull Duration timeToLive;
  private final @NonNull String activeKeyId;
  private final @NonNull Map<@NonNull String, byte @NonNull []> signingKeys;

  public HmacCursorTokenCodec(
      @NonNull ObjectMapper objectMapper,
      @NonNull Clock clock,
      @NonNull Duration timeToLive,
      @NonNull String activeKeyId,
      @NonNull Map<@NonNull String, byte @NonNull []> signingKeys
  ) {
    this.objectMapper = requireNonNull(objectMapper, "cursor object mapper cannot be null");
    this.clock = requireNonNull(clock, "cursor clock cannot be null");
    this.timeToLive = requireNonNull(timeToLive, "cursor time to live cannot be null");
    this.activeKeyId = requireNonNull(activeKeyId, "active cursor signing key id cannot be null");
    this.signingKeys = copyKeys(signingKeys);

    if (timeToLive.isZero() || timeToLive.isNegative()) {
      throw new CursorCodecConfigurationException("cursor time to live must be positive");
    }
    if (!VALID_KEY_ID.matcher(activeKeyId).matches()) {
      throw new CursorCodecConfigurationException("active cursor signing key id is invalid");
    }
    if (!this.signingKeys.containsKey(activeKeyId)) {
      throw new CursorCodecConfigurationException("active cursor signing key is not configured");
    }
  }

  @Override
  public <K extends @NonNull Object> @NonNull String encode(
      @NonNull CursorBinding binding,
      @NonNull K key
  ) {
    requireNonNull(binding, "cursor binding cannot be null");
    requireNonNull(key, "cursor key cannot be null");

    try {
      final var keyJson = objectMapper.writeValueAsString(key);
      final var payload = new CursorPayload(
          binding.scope().value(),
          queryFingerprint(binding),
          clock.instant().plus(timeToLive).getEpochSecond(),
          keyJson
      );
      final var encodedPayload = ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
      final var signedContent = VERSION + "." + activeKeyId + "." + encodedPayload;
      final var activeSigningKey = requireNonNull(
          signingKeys.get(activeKeyId),
          "active cursor signing key is missing"
      );
      final var signature = ENCODER.encodeToString(sign(signedContent, activeSigningKey));
      return signedContent + "." + signature;
    } catch (JacksonException exception) {
      throw new CursorTokenProcessingException("cannot serialize cursor token", exception);
    }
  }

  @Override
  public <K extends @NonNull Object> @NonNull K decode(
      @NonNull String token,
      @NonNull CursorBinding expectedBinding,
      @NonNull Class<K> keyType
  ) {
    requireNonNull(token, "cursor token cannot be null");
    requireNonNull(expectedBinding, "expected cursor binding cannot be null");
    requireNonNull(keyType, "cursor key type cannot be null");

    if (token.isBlank() || token.length() > MAX_TOKEN_LENGTH) {
      throw new MalformedCursorException();
    }

    try {
      final var parts = token.split("\\.", -1);
      if (parts.length != 4 || !VERSION.equals(parts[0])) {
        throw new MalformedCursorException();
      }

      final var signingKey = signingKeys.get(parts[1]);
      if (signingKey == null) {
        throw new MalformedCursorException();
      }

      final var signedContent = String.join(".", parts[0], parts[1], parts[2]);
      final var suppliedSignature = DECODER.decode(parts[3]);
      final var expectedSignature = sign(signedContent, signingKey);
      if (!MessageDigest.isEqual(expectedSignature, suppliedSignature)) {
        throw new MalformedCursorException();
      }

      final var payload = requireNonNull(
          objectMapper.readValue(DECODER.decode(parts[2]), CursorPayload.class),
          "decoded cursor payload cannot be null"
      );
      validatePayload(payload, expectedBinding);
      return requireNonNull(
          objectMapper.readValue(payload.keyJson(), keyType),
          "decoded cursor key cannot be null"
      );
    } catch (MalformedCursorException exception) {
      throw exception;
    } catch (IllegalArgumentException | JacksonException exception) {
      throw new MalformedCursorException(exception);
    }
  }

  private void validatePayload(
      @NonNull CursorPayload payload,
      @NonNull CursorBinding expectedBinding
  ) {
    if (!expectedBinding.scope().value().equals(payload.scope())
        || !queryFingerprint(expectedBinding).equals(payload.queryFingerprint())
        || !clock.instant().isBefore(Instant.ofEpochSecond(payload.expiresAtEpochSecond()))) {
      throw new MalformedCursorException();
    }
  }

  private static @NonNull String queryFingerprint(@NonNull CursorBinding binding) {
    final var digest = messageDigest();
    for (final var component : binding.components()) {
      final var bytes = component.getBytes(StandardCharsets.UTF_8);
      digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
      digest.update(bytes);
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  private static @NonNull MessageDigest messageDigest() {
    try {
      return MessageDigest.getInstance(FINGERPRINT_ALGORITHM);
    } catch (NoSuchAlgorithmException exception) {
      throw new CursorTokenProcessingException("cannot compute cursor query fingerprint", exception);
    }
  }

  private static byte @NonNull [] sign(
      @NonNull String content,
      byte @NonNull [] signingKey
  ) {
    try {
      final var mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(signingKey, HMAC_ALGORITHM));
      return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
    } catch (GeneralSecurityException exception) {
      throw new CursorTokenProcessingException("cannot compute cursor signature", exception);
    }
  }

  private static @NonNull Map<@NonNull String, byte @NonNull []> copyKeys(
      @NonNull Map<@NonNull String, byte @NonNull []> keys
  ) {
    requireNonNull(keys, "cursor signing keys cannot be null");
    final var copy = new HashMap<String, byte[]>();
    keys.forEach((keyId, key) -> {
      requireNonNull(keyId, "cursor signing key id cannot be null");
      requireNonNull(key, "cursor signing key cannot be null");
      if (!VALID_KEY_ID.matcher(keyId).matches()) {
        throw new CursorCodecConfigurationException("cursor signing key id is invalid");
      }
      if (key.length < MINIMUM_KEY_BYTES) {
        throw new CursorCodecConfigurationException("cursor signing key must contain at least 32 bytes");
      }
      copy.put(keyId, key.clone());
    });
    return Map.copyOf(copy);
  }

  private record CursorPayload(
      @NonNull String scope,
      @NonNull String queryFingerprint,
      long expiresAtEpochSecond,
      @NonNull String keyJson
  ) {

    private CursorPayload {
      requireNonNull(scope, "cursor payload scope cannot be null");
      requireNonNull(queryFingerprint, "cursor payload query fingerprint cannot be null");
      requireNonNull(keyJson, "cursor payload key cannot be null");
    }
  }
}
