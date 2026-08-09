package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.cursor;

import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphCriteria;
import org.craft.mcs.cap.shared.application.query.MalformedCursorException;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.testing.TestCursorTokenCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AppleVarietySugarAcidityCursorAdapterTest {

  private static final AppleVarietyAnalysisFilters FILTERS =
      new AppleVarietyAnalysisFilters(new TextSearchCriteria("Kingston"));
  private static final AppleVarietyCatalogPagePosition POSITION =
      new AppleVarietyCatalogPagePosition(
          "Kingston Black",
          UUID.fromString("019bfcc0-4830-7000-8000-000000000001")
      );

  private final TestCursorTokenCodec cursorTokenCodec = new TestCursorTokenCodec();
  private final AppleVarietySugarAcidityCursorAdapter adapter =
      new AppleVarietySugarAcidityCursorAdapter(cursorTokenCodec);

  @Test
  void roundTripsTypedPositionThroughOpaqueToken() {
    final var token = adapter.encode(POSITION, FILTERS, null);

    assertThat(adapter.decode(token, FILTERS, null))
        .isEqualTo(POSITION);
  }

  @Test
  void rejectsTokenWhenNormalizedFiltersChange() {
    final var token = adapter.encode(POSITION, FILTERS, null);
    final var differentFilters = new AppleVarietyAnalysisFilters(new TextSearchCriteria("Dabinett"));

    assertThatThrownBy(() -> adapter.decode(token, differentFilters, null))
        .isInstanceOf(MalformedCursorException.class);
  }

  @Test
  void treatsBlankTransportCursorAsFirstPage() {
    assertThat(adapter.decode("  ", FILTERS, null))
        .isNull();
  }

  @Test
  void rejectsTokenWhenGraphCriteriaChange() {
    final var graphCriteria = new JolicoeurSugarAcidityGraphCriteria(null, true);
    final var token = adapter.encode(POSITION, FILTERS, graphCriteria);

    assertThatThrownBy(() -> adapter.decode(
        token,
        FILTERS,
        new JolicoeurSugarAcidityGraphCriteria(null, false)
    )).isInstanceOf(MalformedCursorException.class);
  }
}
