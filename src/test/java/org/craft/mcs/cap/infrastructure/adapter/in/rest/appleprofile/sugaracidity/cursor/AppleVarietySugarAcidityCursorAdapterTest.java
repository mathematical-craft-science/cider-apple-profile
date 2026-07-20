package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.cursor;

import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietySugarAcidityPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietyPointFilters;
import org.craft.mcs.cap.shared.application.query.MalformedCursorException;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.testing.TestCursorTokenCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AppleVarietySugarAcidityCursorAdapterTest {

  private static final AppleVarietyPointFilters FILTERS = new AppleVarietyPointFilters(
      new TextSearchCriteria("Kingston"),
      null,
      null
  );
  private static final AppleVarietySugarAcidityPagePosition POSITION =
      new AppleVarietySugarAcidityPagePosition(
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
    final var differentFilters = new AppleVarietyPointFilters(
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
