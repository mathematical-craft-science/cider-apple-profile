package org.craft.mcs.cap.appleprofile.analysis.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AppleVarietySugarAcidityZonePointQueryTest {

  @Test
  void firstPageCreatesEmptyFiltersWithDefaultPage() {
    final var query = AppleVarietySugarAcidityZonePointQuery.firstPage();

    assertThat(query)
        .returns(null, value -> value.filters().nameContains())
        .returns(null, AppleVarietySugarAcidityZonePointQuery::graphCriteria)
        .returns(CursorPageRequest.firstPage(), AppleVarietySugarAcidityZonePointQuery::page);
  }
}
