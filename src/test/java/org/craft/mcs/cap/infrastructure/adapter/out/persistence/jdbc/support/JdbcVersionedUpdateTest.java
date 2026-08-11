package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support;

import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JdbcVersionedUpdateTest {

  @Test
  void completesExactlyOneAtomicUpdateWithNextRevision() {
    final var attempted = new VersionedAggregate<>("aggregate", AggregateRevision.of(7));

    assertThat(JdbcVersionedUpdate.complete("test aggregate", attempted, 1))
        .returns("aggregate", VersionedAggregate::aggregate)
        .extracting(VersionedAggregate::revision)
        .isEqualTo(AggregateRevision.of(8));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 2})
  void rejectsAnyUpdateCountOtherThanOne(int affectedRows) {
    assertThatThrownBy(() -> JdbcVersionedUpdate.requireSingleRow("test aggregate", affectedRows))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: test aggregate");
  }
}
