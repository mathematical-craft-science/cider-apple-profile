package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import org.jspecify.annotations.NonNull;


/** Encodes and verifies opaque transport tokens for strongly typed keyset positions. */
public interface CursorTokenCodec {

  <K extends @NonNull Object> @NonNull String encode(
      @NonNull CursorBinding binding,
      @NonNull K key
  );

  <K extends @NonNull Object> @NonNull K decode(
      @NonNull String token,
      @NonNull CursorBinding expectedBinding,
      @NonNull Class<K> keyType
  );
}
