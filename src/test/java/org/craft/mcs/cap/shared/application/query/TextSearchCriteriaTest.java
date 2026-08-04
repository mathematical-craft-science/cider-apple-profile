package org.craft.mcs.cap.shared.application.query;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class TextSearchCriteriaTest {

  @Test
  void normalizesInput() {
    assertThat(new TextSearchCriteria("  Kingston  ").query()).isEqualTo("Kingston");
  }

  @Test
  void rejectsBlankInput() {
    assertThatThrownBy(() -> new TextSearchCriteria(" "))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("cannot be blank");
  }
}
