package org.craft.mcs.cap.core.feedstock.domain.model.batch.value;

import java.time.LocalDate;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record FeedstockSeason(
    @NonNull LocalDate startsOn,
    @NonNull LocalDate endsOn
) {

  public FeedstockSeason {
    requireNonNull(startsOn, "feedstock season start date cannot be null");
    requireNonNull(endsOn, "feedstock season end date cannot be null");

    if (endsOn.isBefore(startsOn)) {
      throw new IllegalArgumentException("feedstock season end date cannot be before start date");
    }
  }

  public static @NonNull FeedstockSeason of(
      @NonNull LocalDate startsOn,
      @NonNull LocalDate endsOn
  ) {
    return new FeedstockSeason(startsOn, endsOn);
  }

  public boolean contains(@NonNull LocalDate date) {
    requireNonNull(date, "feedstock season date cannot be null");
    return !date.isBefore(startsOn) && !date.isAfter(endsOn);
  }
}
