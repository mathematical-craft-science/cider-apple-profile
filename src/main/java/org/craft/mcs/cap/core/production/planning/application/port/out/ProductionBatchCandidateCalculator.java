package org.craft.mcs.cap.core.production.planning.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.production.planning.application.contract.AvailableProductionSource;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest;
import org.jspecify.annotations.NonNull;


/** Outbound boundary from production planning to reusable formulation calculations. */
public interface ProductionBatchCandidateCalculator {

  @NonNull ProductionBatchCandidateCalculation calculate(
      @NonNull List<@NonNull AvailableProductionSource> sources,
      @NonNull ProductionBatchCandidateCalculationRequest request
  );
}
