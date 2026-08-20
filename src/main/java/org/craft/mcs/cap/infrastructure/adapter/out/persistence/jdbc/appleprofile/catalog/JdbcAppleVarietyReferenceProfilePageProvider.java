package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.catalog;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfilePageQuery;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfileView;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.out.AppleVarietyReferenceProfilePageProvider;
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
class JdbcAppleVarietyReferenceProfilePageProvider
    implements AppleVarietyReferenceProfilePageProvider {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull List<@NonNull AppleVarietyReferenceProfileView> findProfiles(
      @NonNull AppleVarietyReferenceProfilePageQuery query
  ) {
    requireNonNull(query, "apple variety reference profile page query cannot be null");

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
        (resultSet, rowNumber) -> AppleVarietyReferenceProfileRowMapper.toView(resultSet)
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
