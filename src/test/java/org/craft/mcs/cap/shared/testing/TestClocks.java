package org.craft.mcs.cap.shared.testing;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.jspecify.annotations.NonNull;


public final class TestClocks {

  public static final @NonNull Clock FEEDSTOCK_MEASUREMENT_CLOCK =
      Clock.fixed(Instant.parse("2000-01-15T10:00:00Z"), ZoneOffset.UTC);

  public static final @NonNull Clock FUTURE_CLOCK =
      Clock.fixed(Instant.parse("2999-01-15T10:00:00Z"), ZoneOffset.UTC);

  private TestClocks() {
  }

  public static @NonNull Instant feedstockMeasurementInstant() {
    return Instant.now(FEEDSTOCK_MEASUREMENT_CLOCK);
  }

  public static @NonNull Instant feedstockMeasurementInstantPlusSeconds(long seconds) {
    return feedstockMeasurementInstant().plusSeconds(seconds);
  }

  public static @NonNull Instant futureInstant() {
    return Instant.now(FUTURE_CLOCK);
  }
}
