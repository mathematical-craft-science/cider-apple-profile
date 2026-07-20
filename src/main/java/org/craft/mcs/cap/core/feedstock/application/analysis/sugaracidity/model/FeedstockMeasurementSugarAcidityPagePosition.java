package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable keyset position for feedstock measurement sugar-acidity queries. */
public record FeedstockMeasurementSugarAcidityPagePosition(
    @NonNull Instant measuredAt,
    @NonNull UUID measurementId
) {

  public FeedstockMeasurementSugarAcidityPagePosition {
    requireNonNull(measuredAt, "feedstock measurement page position measuredAt cannot be null");
    requireNonNull(measurementId, "feedstock measurement page position id cannot be null");
  }
}
