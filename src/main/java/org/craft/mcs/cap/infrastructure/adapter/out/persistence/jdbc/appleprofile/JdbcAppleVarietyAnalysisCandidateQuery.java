package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateCriteria;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateView;
import org.craft.mcs.cap.shared.platform.persistence.jdbc.SqlQueryBuilder;
import org.craft.mcs.cap.shared.platform.persistence.jdbc.SqlQueryBuilder.SqlQueryOption;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JdbcAppleVarietyAnalysisCandidateQuery
    implements AppleVarietyAnalysisCandidateQuery {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull List<@NonNull AppleVarietyAnalysisCandidateView> findCandidates(
      @NonNull AppleVarietyAnalysisCandidateCriteria criteria
  ) {
    requireNonNull(criteria, "apple variety analysis candidate criteria cannot be null");

    final var cursor = criteria.after();
    final var nameContains = normalizeNameContains(criteria.nameContains());

    final var sqlQuery = SqlQueryBuilder.from("""
        SELECT *
        FROM apple_varieties
        WHERE TRUE
        """)
        .with(nameContains(nameContains))
        .with(afterCursor(cursor))
        .orderBy("name ASC, variety_id ASC")
        .limit(criteria.limit())
        .build();

    return jdbcTemplate.query(
        sqlQuery.sql(),
        sqlQuery.parameters(),
        (resultSet, rowNumber) -> AppleVarietyAnalysisCandidateRowMapper.toView(resultSet)
    );
  }

  private static @Nullable String normalizeNameContains(@Nullable TextSearchCriteria criteria) {
    if (criteria == null) {
      return null;
    }

    final var normalized = criteria.query().strip().toLowerCase(Locale.ROOT);
    return normalized.isBlank() ? null : normalized;
  }

  private static @NonNull SqlQueryOption nameContains(@Nullable String nameContains) {
    return builder -> {
      if (nameContains == null) {
        return;
      }

      builder
          .where("LOWER(name) LIKE CONCAT('%', :nameContains, '%')")
          .parameter("nameContains", nameContains);
    };
  }

  private static @NonNull SqlQueryOption afterCursor(
      @Nullable AppleVarietyCatalogPagePosition cursor
  ) {
    return builder -> {
      if (cursor == null) {
        return;
      }

      builder
          .where("name > :afterName OR (name = :afterName AND variety_id > :afterVarietyId)")
          .parameter("afterName", cursor.name())
          .parameter("afterVarietyId", cursor.varietyId());
    };
  }
}
