package org.craft.mcs.cap.core.production.planning.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.production.planning.application.model.AvailableProductionSource;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateCalculationRequest;
import org.jspecify.annotations.NonNull;


/** Outbound boundary from production planning to reusable profile-analysis calculations. */
public interface ProductionBatchCandidateCalculator {

  @NonNull ProductionBatchCandidateCalculation calculate(
      @NonNull List<@NonNull AvailableProductionSource> sources,
      @NonNull ProductionBatchCandidateCalculationRequest request
  );
}
