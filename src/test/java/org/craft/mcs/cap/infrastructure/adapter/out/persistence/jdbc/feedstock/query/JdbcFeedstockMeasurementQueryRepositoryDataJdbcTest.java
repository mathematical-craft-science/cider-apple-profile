package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresDataJdbcTestConfiguration.class)
class JdbcFeedstockMeasurementQueryRepositoryDataJdbcTest {

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Test
  void returnsNoSummariesWhenFeedstockBatchDoesNotExist() {
    final var measurementSetQuery = new JdbcFeedstockMeasurementSetQuery(jdbcTemplate);
    final var repository = new JdbcFeedstockMeasurementQueryRepository(measurementSetQuery);

    assertThat(repository.findSummariesByBatchId(FeedstockBatchId.newId())).isEmpty();
  }
}
