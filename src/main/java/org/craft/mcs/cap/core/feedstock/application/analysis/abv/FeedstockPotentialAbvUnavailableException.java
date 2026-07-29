package org.craft.mcs.cap.core.feedstock.application.analysis.abv;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.profileanalysis.capability.application.ProfileAnalysisInputUnavailableException;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisInputAvailability;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability.ABV_PREDICTION;


/** Indicates that the current feedstock state cannot support potential ABV prediction. */
@SuppressWarnings("java:S110")
public final class FeedstockPotentialAbvUnavailableException
    extends ProfileAnalysisInputUnavailableException {

  @Serial
  private static final long serialVersionUID = 1L;

  public FeedstockPotentialAbvUnavailableException(
      @NonNull FeedstockBatchId batchId,
      @NonNull ProfileAnalysisInputAvailability availability
  ) {
    super(
        "current feedstock state requires Specific Gravity evidence for potential ABV prediction: "
            + requireNonNull(batchId, "feedstock batch id cannot be null").value(),
        ABV_PREDICTION,
        availability
    );
  }
}
