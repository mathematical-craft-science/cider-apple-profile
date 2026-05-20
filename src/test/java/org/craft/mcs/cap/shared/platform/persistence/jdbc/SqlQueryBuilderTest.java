package org.craft.mcs.cap.shared.platform.persistence.jdbc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class SqlQueryBuilderTest {

  @Test
  void buildsQueryWithOptionalPredicatesAndParameters() {
    final var query = SqlQueryBuilder.from("""
        select *
        from feedstock_measurements sample
        where sample.batch_id = :batchId
        """)
        .with(builder -> builder
            .where("sample.measured_at > :measuredAt")
            .parameter("measuredAt", "2026-01-01T00:00:00Z"))
        .orderBy("sample.measured_at asc")
        .limit(50)
        .parameter("batchId", "batch-1")
        .build();

    assertThat(query.sql()).isEqualTo("""
        select *
        from feedstock_measurements sample
        where sample.batch_id = :batchId
          and (sample.measured_at > :measuredAt)
        order by sample.measured_at asc
        limit :limit
        """);
    assertThat(query.parameters().getValue("batchId")).isEqualTo("batch-1");
    assertThat(query.parameters().getValue("measuredAt")).isEqualTo("2026-01-01T00:00:00Z");
    assertThat(query.parameters().getValue("limit")).isEqualTo(50);
  }
}
