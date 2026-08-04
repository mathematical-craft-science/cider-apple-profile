package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockAnalysisCandidateSubjectQuery;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSubject;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileSubjectQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads Feedstock batch ownership scopes without projecting cross-context measurement evidence. */
@Component
@RequiredArgsConstructor
final class JdbcFeedstockCurrentProfileSubjectQuery
    implements FeedstockCurrentProfileSubjectQuery, FeedstockAnalysisCandidateSubjectQuery {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileSubject> findCandidates(
      @NonNull CurrentFeedstockBatchAnalysisCandidateQuery query
  ) {
    requireNonNull(query, "current feedstock batch analysis candidate query cannot be null");
    final var sql = new StringBuilder("""
        SELECT batch.batch_id,
               batch.workspace_id
          FROM feedstock_batches batch
         WHERE batch.workspace_id = :workspaceId
           AND batch.deleted_at IS NULL
        """);
    final var parameters = new MapSqlParameterSource("workspaceId", query.workspaceId().value());
    if (query.batch() != null) {
      sql.append(" AND batch.batch_id IN (:batchIds)");
      parameters.addValue(
          "batchIds",
          query.batch().batchIds().stream().map(FeedstockBatchId::value).toList() // NOSONAR
      );
    }
    if (query.season() != null) {
      sql.append("""
           AND batch.season_starts_on <= :seasonEndsOn
           AND batch.season_ends_on >= :seasonStartsOn
          """);
      parameters
          .addValue("seasonStartsOn", query.season().startsOn()) // NOSONAR
          .addValue("seasonEndsOn", query.season().endsOn()); // NOSONAR
    }
    sql.append(" ORDER BY batch.batch_id");

    // SQL structure is composed only from internal constant fragments; values remain bound parameters.
    //noinspection SqlSourceToSinkFlow
    return jdbcTemplate.query(sql.toString(), parameters, JdbcFeedstockCurrentProfileSubjectQuery::map); // NOSONAR
  }

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileSubject> findByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    if (batchIds.isEmpty()) {
      return List.of();
    }
    return jdbcTemplate.query(
        """
            SELECT batch_id,
                   workspace_id
              FROM feedstock_batches
             WHERE batch_id IN (:batchIds)
               AND deleted_at IS NULL
             ORDER BY batch_id
            """,
        new MapSqlParameterSource(
            "batchIds",
            batchIds.stream().map(FeedstockBatchId::value).toList()
        ),
        JdbcFeedstockCurrentProfileSubjectQuery::map
    );
  }

  private static @NonNull FeedstockCurrentProfileSubject map(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    return new FeedstockCurrentProfileSubject(
        FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class))
    );
  }
}
