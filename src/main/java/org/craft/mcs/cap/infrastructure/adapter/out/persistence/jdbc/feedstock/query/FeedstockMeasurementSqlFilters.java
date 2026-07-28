package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;


/** Appends common Feedstock filters to measurement read queries using their stable SQL aliases. */
final class FeedstockMeasurementSqlFilters {

  private FeedstockMeasurementSqlFilters() {
  }

  static void append(
      @NonNull StringBuilder sql,
      @NonNull MapSqlParameterSource parameters,
      @Nullable WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season
  ) {
    if (workspaceId != null) {
      sql.append(" AND s.workspace_id = :workspaceId");
      parameters.addValue("workspaceId", workspaceId.value());
    }
    if (batchCriteria != null) {
      sql.append(" AND s.subject_id IN (:batchIds)");
      parameters.addValue(
          "batchIds",
          batchCriteria.batchIds().stream().map(FeedstockBatchId::value).toList()
      );
    }
    if (season != null) {
      sql.append("""
           AND batch.season_starts_on <= :seasonEndsOn
           AND batch.season_ends_on >= :seasonStartsOn
          """);
      parameters
          .addValue("seasonStartsOn", season.startsOn())
          .addValue("seasonEndsOn", season.endsOn());
    }
  }
}
