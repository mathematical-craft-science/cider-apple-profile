package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetMetadataUseCase;
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
import static org.mockito.Mockito.verifyNoInteractions;


class JdbcFeedstockCurrentProfileQueryTest {

  @Test
  void readsOnlyFeedstockOwnedTablesBeforeProjectingCurrentProfiles() {
    final var jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    final var projector = mock(ProjectCurrentMaterialStateUseCase.class);
    final var metadataQuery = mock(QueryMeasurementSetMetadataUseCase.class);
    final var repository = new JdbcFeedstockCurrentProfileQuery(
        jdbcTemplate,
        projector,
        metadataQuery
    );
    final var batchId = FeedstockBatchId.newId();
    final var position = new FeedstockAnalysisCandidatePagePosition(
        Instant.parse("2026-07-01T12:00:00Z"),
        batchId.value()
    );
    doReturn(List.of())
        .when(jdbcTemplate)
        .query(anyString(), any(SqlParameterSource.class), feedstockBatchIdRowMapper());

    repository.findCandidates(new CurrentFeedstockBatchAnalysisCandidateQuery(
        WorkspaceId.newId(),
        FeedstockBatchCriteria.of(batchId),
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        position,
        25
    ));

    final var sql = ArgumentCaptor.forClass(String.class);
    final var parameters = ArgumentCaptor.forClass(SqlParameterSource.class);
    verify(jdbcTemplate).query(sql.capture(), parameters.capture(), feedstockBatchIdRowMapper());

    assertThat(sql.getValue())
        .contains("batch.batch_id IN (:batchIds)")
        .contains("batch.season_starts_on <= :seasonEndsOn")
        .doesNotContain("batch_trace_step_subjects")
        .doesNotContain("measurement_sets");
    assertThat(parameters.getValue())
        .satisfies(value -> assertThat(value.getValue("batchIds")).isEqualTo(List.of(batchId.value())));
    verifyNoInteractions(projector, metadataQuery);
  }

  @SuppressWarnings("unchecked")
  private static RowMapper<FeedstockBatchId> feedstockBatchIdRowMapper() {
    return isA(RowMapper.class);
  }
}
