package org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.FermentationStartReadinessView;
import org.jspecify.annotations.NonNull;


/**
 * Assesses readiness immediately before fermentation start.
 */
public interface AssessFermentationStartReadinessUseCase {

  @NonNull
  FermentationStartReadinessView assess(@NonNull FermentationStartReadinessCommand command);
}
