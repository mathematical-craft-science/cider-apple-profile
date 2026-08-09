package org.craft.mcs.cap.core.feedstock.application.analysis.abv;

import java.io.Serial;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.profileanalysis.input.application.ProfileAnalysisInputUnavailableException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;


/** Indicates that the current feedstock state cannot support potential ABV prediction. */
@SuppressWarnings("java:S110")
public final class FeedstockPotentialAbvUnavailableException
    extends ProfileAnalysisInputUnavailableException {

  @Serial
  private static final long serialVersionUID = 1L;

  public FeedstockPotentialAbvUnavailableException(
      @NonNull FeedstockBatchId batchId
  ) {
    super(
        "current feedstock state requires Specific Gravity evidence for potential ABV prediction: "
            + requireNonNull(batchId, "feedstock batch id cannot be null").value(),
        "POTENTIAL_ABV_PREDICTION",
        Set.of(SPECIFIC_GRAVITY),
        Set.<MeasurementCharacteristic>of()
    );
  }
}
