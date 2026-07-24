package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.mapper;

import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps the legacy Feedstock measurement history projection to its deprecated response contract. */
public final class FeedstockMeasurementRestMapper {

  private FeedstockMeasurementRestMapper() {
  }

  public static @NonNull FeedstockMeasurementResponse toResponse(
      @NonNull FeedstockMeasurementSummary measurement
  ) {
    requireNonNull(measurement, "feedstock measurement summary cannot be null");
    final var profile = ChemicalProfileRestMapper.toResponse(measurement.chemicalProfile());
    return new FeedstockMeasurementResponse(
        measurement.measurementId().value(),
        measurement.workspaceId().value(),
        measurement.batchId().value(),
        measurement.name().value(),
        measurement.measuredAt(),
        new FeedstockMeasurementChemicalProfileResponse(
            profile.sugar(),
            profile.ph(),
            profile.titratableAcidity(),
            profile.tannin(),
            profile.yeastAssimilableNitrogen(),
            null,
            null
        ),
        measurement.note()
    );
  }
}
