package org.craft.mcs.cap.core.feedstock.application.workspace.port.in;

import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockWorkspacePotentialAbvView(
    @NonNull MeasurementValueKind originalGravityValueKind,
    @NonNull PotentialAbvPrediction prediction
) {

  public FeedstockWorkspacePotentialAbvView {
    requireNonNull(originalGravityValueKind, "original gravity value kind cannot be null");
    requireNonNull(prediction, "potential ABV prediction cannot be null");
  }
}
