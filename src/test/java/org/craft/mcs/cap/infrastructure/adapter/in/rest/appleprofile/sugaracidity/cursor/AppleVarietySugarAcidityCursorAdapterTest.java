package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.cursor;

import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.shared.application.query.MalformedCursorException;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.testing.TestCursorTokenCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AppleVarietySugarAcidityCursorAdapterTest {

  private static final AppleVarietyAnalysisFilters FILTERS = new AppleVarietyAnalysisFilters(
      new TextSearchCriteria("Kingston"),
      null,
      null
  );
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
    final var token = adapter.encode(POSITION, FILTERS);

    assertThat(adapter.decode(token, FILTERS))
        .isEqualTo(POSITION);
  }

  @Test
  void rejectsTokenWhenNormalizedFiltersChange() {
    final var token = adapter.encode(POSITION, FILTERS);
    final var differentFilters = new AppleVarietyAnalysisFilters(
        new TextSearchCriteria("Dabinett"),
        null,
        null
    );

    assertThatThrownBy(() -> adapter.decode(token, differentFilters))
        .isInstanceOf(MalformedCursorException.class);
  }

  @Test
  void treatsBlankTransportCursorAsFirstPage() {
    assertThat(adapter.decode("  ", FILTERS))
        .isNull();
  }
}
