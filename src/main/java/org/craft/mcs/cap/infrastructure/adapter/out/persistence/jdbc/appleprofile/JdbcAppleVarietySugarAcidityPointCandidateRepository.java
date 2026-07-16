package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateRepository;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateCursor;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateView;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.entity.AppleVarietyJdbcMapper;
import org.craft.mcs.cap.shared.platform.persistence.jdbc.SqlQueryBuilder;
import org.craft.mcs.cap.shared.platform.persistence.jdbc.SqlQueryBuilder.SqlQueryOption;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JdbcAppleVarietySugarAcidityPointCandidateRepository
    implements AppleVarietySugarAcidityPointCandidateRepository {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull List<@NonNull AppleVarietySugarAcidityPointCandidateView> findCandidates(
      @NonNull AppleVarietySugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "apple variety point candidate query cannot be null");

    final var cursor = query.after();
    final var nameContains = normalizeNameContains(query.nameContains());

    final var sqlQuery = SqlQueryBuilder.from("""
        SELECT *
        FROM apple_varieties
        WHERE TRUE
        """)
        .with(nameContains(nameContains))
        .with(afterCursor(cursor))
        .orderBy("name ASC, variety_id ASC")
        .limit(query.limit())
        .build();

    return jdbcTemplate.query(
            sqlQuery.sql(),
            sqlQuery.parameters(),
            (resultSet, rowNumber) -> AppleVarietyJdbcMapper.toCandidateView(
                AppleVarietyJdbcMapper.fromResultSet(resultSet)
            )
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
      @Nullable AppleVarietySugarAcidityPointCandidateCursor cursor
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
