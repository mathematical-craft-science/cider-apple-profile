package org.craft.mcs.cap.shared.application.query.pagination;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CursorPageRequestTest {

  @Test
  void retainsTypedPagePosition() {
    final var request = new CursorPageRequest<>(42, 25);

    assertThat(request)
        .returns(42, CursorPageRequest::after)
        .returns(25, CursorPageRequest::limit);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, QueryLimits.MAXIMUM_PAGE_FETCH})
  void rejectsLimitOutsideAllowedRange(int limit) {
    assertThatThrownBy(() -> new CursorPageRequest<>(null, limit))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("between 1 and");
  }
}
