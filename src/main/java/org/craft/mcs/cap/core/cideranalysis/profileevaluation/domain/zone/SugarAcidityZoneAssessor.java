package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone;

import java.util.Optional;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;


public interface SugarAcidityZoneAssessor {

  @NonNull
  Optional<@NonNull SugarAcidityZoneAssessment> assess(@NonNull ChemicalProfileAxes axes);
}
