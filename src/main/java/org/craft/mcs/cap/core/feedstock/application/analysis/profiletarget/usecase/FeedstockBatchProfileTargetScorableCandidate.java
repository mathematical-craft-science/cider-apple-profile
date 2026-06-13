package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase;

import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


record FeedstockBatchProfileTargetScorableCandidate(
    @NonNull FeedstockMeasurement measurement,
    @NonNull ChemicalProfileAxes axes,
    @NonNull ProfileTargetDistanceCandidate candidate
) {

  FeedstockBatchProfileTargetScorableCandidate {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    requireNonNull(axes, "chemical profile axes cannot be null");
    requireNonNull(candidate, "profile target distance candidate cannot be null");
  }
}
