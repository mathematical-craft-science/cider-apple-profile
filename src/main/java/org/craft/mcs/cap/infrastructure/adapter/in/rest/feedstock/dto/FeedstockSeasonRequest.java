package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.time.LocalDate;
import org.jspecify.annotations.NonNull;


public record FeedstockSeasonRequest(
    @NonNull LocalDate startsOn,
    @NonNull LocalDate endsOn
) {
}
