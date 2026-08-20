package org.craft.mcs.cap.core.feedstock.planning.application.composition.port.out;

import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationView;
import org.jspecify.annotations.NonNull;


/** Outbound boundary from Feedstock Planning to reusable formulation optimization. */
public interface FeedstockCompositionOptimizer {

  @NonNull FeedstockCompositionOptimizationView optimize(
      @NonNull FeedstockCompositionOptimizationRequest request
  );
}
