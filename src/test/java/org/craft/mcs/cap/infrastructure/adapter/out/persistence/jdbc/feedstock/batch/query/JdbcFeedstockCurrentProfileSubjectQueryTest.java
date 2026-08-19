package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSubject;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class JdbcFeedstockCurrentProfileSubjectQueryTest {

  @Test
  void readsOnlyFeedstockOwnedTablesWhenSelectingAnalysisSubjects() {
    final var jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    final var queryAdapter = new JdbcFeedstockCurrentProfileSubjectQuery(jdbcTemplate);
    final var batchId = FeedstockBatchId.newId();
    final var position = new FeedstockCurrentProfilePagePosition(
        Instant.parse("2026-07-01T12:00:00Z"),
        batchId.value()
    );
    doReturn(List.of())
        .when(jdbcTemplate)
        .query(anyString(), any(SqlParameterSource.class), subjectRowMapper());

    queryAdapter.findSubjects(new FeedstockCurrentProfilePageQuery(
        WorkspaceId.newId(),
        FeedstockBatchCriteria.of(batchId),
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        position,
        25
    ));

    final var sql = ArgumentCaptor.forClass(String.class);
    final var parameters = ArgumentCaptor.forClass(SqlParameterSource.class);
    verify(jdbcTemplate).query(sql.capture(), parameters.capture(), subjectRowMapper());

    assertThat(sql.getValue())
        .contains("batch.batch_id IN (:batchIds)")
        .contains("batch.season_starts_on <= :seasonEndsOn")
        .doesNotContain("batch_trace_step_subjects")
        .doesNotContain("measurement_sets");
    assertThat(parameters.getValue())
        .satisfies(value -> assertThat(value.getValue("batchIds")).isEqualTo(List.of(batchId.value())));
  }

  @SuppressWarnings("unchecked")
  private static RowMapper<FeedstockCurrentProfileSubject> subjectRowMapper() {
    return isA(RowMapper.class);
  }
}
