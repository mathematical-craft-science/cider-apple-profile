package org.craft.mcs.cap.shared.testing;

import java.util.HashMap;
import org.craft.mcs.cap.shared.application.query.MalformedCursorException;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorBinding;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorTokenCodec;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** In-memory cursor codec for focused REST cursor adapter tests. */
public final class TestCursorTokenCodec implements CursorTokenCodec {

  private final @NonNull HashMap<@NonNull String, @NonNull StoredCursor> cursors = new HashMap<>();
  private int sequence;

  @Override
  public <K extends @NonNull Object> @NonNull String encode(
      @NonNull CursorBinding binding,
      @NonNull K key
  ) {
    requireNonNull(binding, "cursor binding cannot be null");
    requireNonNull(key, "cursor key cannot be null");

    final var token = "test-cursor-" + ++sequence;
    cursors.put(token, new StoredCursor(binding, key));
    return token;
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

    final var stored = cursors.get(token);
    if (stored == null || !stored.binding().equals(expectedBinding) || !keyType.isInstance(stored.key())) {
      throw new MalformedCursorException();
    }
    return keyType.cast(stored.key());
  }

  private record StoredCursor(
      @NonNull CursorBinding binding,
      @NonNull Object key
  ) {
  }
}
