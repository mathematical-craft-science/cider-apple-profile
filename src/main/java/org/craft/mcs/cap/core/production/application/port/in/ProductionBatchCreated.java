package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProductionBatchCreated(
    @NonNull ProductionBatch productionBatch,
    @NonNull CalculateChemicalProfileCompositionResult composition
) {

  public ProductionBatchCreated {
    requireNonNull(productionBatch, "production batch cannot be null");
    requireNonNull(composition, "chemical profile composition result cannot be null");
  }
}
