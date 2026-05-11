package org.craft.mcs.cap.shared.platform.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CursorPageRequestTest {

  @Test
  void normalizesBlankCursorToNull() {
    final var request = new CursorPageRequest("  ", 25);

    assertThat(request.after()).isNull();
    assertThat(request.limit()).isEqualTo(25);
  }

  @Test
  void trimsCursor() {
    final var request = new CursorPageRequest("  cursor-1  ", 25);

    assertThat(request.after()).isEqualTo("cursor-1");
  }

  @Test
  void rejectsLimitOutsideAllowedRange() {
    assertThatThrownBy(() -> new CursorPageRequest(null, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("between 1 and");

    assertThatThrownBy(() -> new CursorPageRequest(null, CursorPageRequest.MAX_LIMIT + 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("between 1 and");
  }
}
