package org.craft.mcs.cap.core.cideranalysis.graph.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphMetadataView;
import org.jspecify.annotations.NonNull;


/** Returns immutable metadata for rendering the configured Jolicoeur SG/TA graph. */
public interface QueryJolicoeurSugarAcidityGraphMetadataUseCase {

  @NonNull JolicoeurSugarAcidityGraphMetadataView query();
}
