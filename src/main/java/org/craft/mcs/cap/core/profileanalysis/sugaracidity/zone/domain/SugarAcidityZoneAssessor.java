package org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain;

import java.util.Optional;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;


/**
 * Domain service interface for assigning sugar-acidity graph zones to canonical chemical axes.
 *
 * <p>Implementations may use different geometries, for example rectangular Jolicoeur threshold
 * rules or ellipse membership based on graph metadata. Returning {@link Optional#empty()} means the
 * profile is not plottable for this assessor, normally because sugar or acidity is missing.
 */
public interface SugarAcidityZoneAssessor {

  @NonNull
  Optional<@NonNull SugarAcidityZoneAssessment> assess(@NonNull ChemicalProfileAxes axes);
}
