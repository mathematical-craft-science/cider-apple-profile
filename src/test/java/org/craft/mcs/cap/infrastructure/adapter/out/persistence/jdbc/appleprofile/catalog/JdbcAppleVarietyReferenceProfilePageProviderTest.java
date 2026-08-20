package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.catalog;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfilePageQuery;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVariety;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class JdbcAppleVarietyReferenceProfilePageProviderTest {

  @Test
  void normalizesNameContainsBeforePassingItToJdbcQuery() {
    final var jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    final var repository = new JdbcAppleVarietyReferenceProfilePageProvider(jdbcTemplate);

    doReturn(List.of())
        .when(jdbcTemplate)
        .query(anyString(), any(SqlParameterSource.class), isAAppleVarietyRowMapper());

    repository.findProfiles(new AppleVarietyReferenceProfilePageQuery(
        new TextSearchCriteria("  Kingston  "),
        null,
        50
    ));

    final var parameters = ArgumentCaptor.forClass(SqlParameterSource.class);
    verify(jdbcTemplate).query(anyString(), parameters.capture(), isAAppleVarietyRowMapper());

    assertThat(parameters.getValue().getValue("nameContains")).isEqualTo("kingston");
    assertThat(parameters.getValue().getValue("limit")).isEqualTo(50);
  }

  @Test
  void passesNullNameContainsWhenSearchCriteriaIsAbsent() {
    final var jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    final var repository = new JdbcAppleVarietyReferenceProfilePageProvider(jdbcTemplate);

    doReturn(List.of())
        .when(jdbcTemplate)
        .query(anyString(), any(SqlParameterSource.class), isAAppleVarietyRowMapper());

    repository.findProfiles(new AppleVarietyReferenceProfilePageQuery(
        null,
        null,
        50
    ));

    final var sql = ArgumentCaptor.forClass(String.class);
    final var parameters = ArgumentCaptor.forClass(SqlParameterSource.class);
    verify(jdbcTemplate).query(sql.capture(), parameters.capture(), isAAppleVarietyRowMapper());

    assertThat(sql.getValue()).doesNotContain("nameContains");
    assertThat(parameters.getValue().hasValue("nameContains")).isFalse();
    assertThat(parameters.getValue().getValue("limit")).isEqualTo(50);
  }

  @Test
  void appliesCursorAfterCurrentNameAndVarietyId() {
    final var jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    final var repository = new JdbcAppleVarietyReferenceProfilePageProvider(jdbcTemplate);
    final var cursorId = UUID.fromString("0194d5df-1111-7000-8000-000000000001");

    doReturn(List.of())
        .when(jdbcTemplate)
        .query(anyString(), any(SqlParameterSource.class), isAAppleVarietyRowMapper());

    repository.findProfiles(new AppleVarietyReferenceProfilePageQuery(
        null,
        new AppleVarietyCatalogPagePosition("Kingston Black", cursorId),
        25
    ));

    final var sql = ArgumentCaptor.forClass(String.class);
    final var parameters = ArgumentCaptor.forClass(SqlParameterSource.class);
    verify(jdbcTemplate).query(sql.capture(), parameters.capture(), isAAppleVarietyRowMapper());

    assertThat(sql.getValue())
        .contains("name > :afterName OR (name = :afterName AND variety_id > :afterVarietyId)")
        .contains("order by name ASC, variety_id ASC");
    assertThat(parameters.getValue().getValue("afterName")).isEqualTo("Kingston Black");
    assertThat(parameters.getValue().getValue("afterVarietyId")).isEqualTo(cursorId);
    assertThat(parameters.getValue().getValue("limit")).isEqualTo(25);
  }

  private static @SuppressWarnings("unchecked") RowMapper<AppleVariety> isAAppleVarietyRowMapper() {
    return isA(RowMapper.class);
  }
}
