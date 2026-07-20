package org.craft.mcs.cap.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AppleVarietySugarAcidityZonePointQueryTest {

  @Test
  void firstPageCreatesEmptyFiltersWithDefaultPage() {
    final var query = AppleVarietySugarAcidityZonePointQuery.firstPage();

    assertThat(query.filters().nameContains()).isNull();
    assertThat(query.filters().sugarAcidityZone()).isNull();
    assertThat(query.filters().capability()).isNull();
    assertThat(query.page()).isEqualTo(CursorPageRequest.firstPage());
  }
}
