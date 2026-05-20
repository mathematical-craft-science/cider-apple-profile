package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support;

import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;


class JdbcPersistenceExceptionTranslatorTest {

  @Test
  void returnsOperationResultWhenNoOptimisticLockingFailureOccurs() {
    final var result = translateOptimisticLocking("feedstock batch", () -> "saved");

    assertThat(result).isEqualTo("saved");
  }

  @Test
  void runsVoidOperationWhenNoOptimisticLockingFailureOccurs() {
    assertThatCode(() -> translateOptimisticLocking("feedstock batch", () -> {
    }))
        .doesNotThrowAnyException();
  }

  @Test
  void translatesOptimisticLockingFailureToApplicationConflict() {
    final var cause = new OptimisticLockingFailureException("stale version");

    assertThatThrownBy(() -> translateOptimisticLocking("feedstock batch", () -> {
      throw cause;
    }))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasCause(cause)
        .satisfies(exception -> assertThat((ConcurrentResourceModificationException) exception)
            .extracting(ConcurrentResourceModificationException::resourceName)
            .isEqualTo("feedstock batch"));
  }
}
