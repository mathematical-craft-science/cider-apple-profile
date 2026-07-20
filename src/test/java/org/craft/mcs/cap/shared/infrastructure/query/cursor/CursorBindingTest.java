package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CursorBindingTest {

  @Test
  void preservesComponentBoundaries() {
    final var scope = CursorScope.of("feedstock.measurements.v1");

    assertThat(CursorBinding.boundTo(scope, "ab", "c"))
        .isEqualTo(CursorBinding.boundTo(scope, "ab", "c"))
        .isNotEqualTo(CursorBinding.boundTo(scope, "a", "bc"));
  }

  @Test
  void protectsComponentsFromCallerMutation() {
    final var scope = CursorScope.of("feedstock.measurements.v1");
    final var components = new String[]{"workspace=one"};

    final var binding = CursorBinding.boundTo(scope, components);
    components[0] = "workspace=two";

    assertThat(binding.components())
        .containsExactly("workspace=one");
  }

  @Test
  void rejectsInvalidCursorScopeWithSemanticException() {
    assertThatThrownBy(() -> CursorScope.of("Invalid Scope"))
        .isInstanceOf(CursorDefinitionException.class)
        .hasMessage("cursor scope is invalid");
  }
}
