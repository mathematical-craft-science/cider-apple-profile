package org.craft.mcs.cap.shared.domain.sugaracidity.assessment;

import java.util.Optional;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.SugarAcidityGraphMetadata;
import org.jspecify.annotations.NonNull;


public interface SugarAcidityZoneAssessor {

  @NonNull
  Optional<@NonNull SugarAcidityZoneAssessment> assess(@NonNull ChemicalProfileAxes axes);

  @NonNull
  SugarAcidityGraphMetadata graphMetadata();
}
