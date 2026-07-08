package org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain;

import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record SugarAcidityProfileEvaluation(
    @NonNull ChemicalProfileAxes axes,
    @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment,
    boolean plottable,
    @Nullable SugarAcidityZoneAssessment assessment
) {

  public SugarAcidityProfileEvaluation {
    requireNonNull(axes, "chemical profile axes cannot be null");
    requireNonNull(capabilityAssessment, "chemical profile capability assessment cannot be null");
    if (plottable && assessment == null) {
      throw new DomainRuleViolationException("plottable sugar-acidity profile must have zone assessment");
    }
    if (!plottable && assessment != null) {
      throw new DomainRuleViolationException("unplottable sugar-acidity profile cannot have zone assessment");
    }
  }

  public @NonNull Optional<@NonNull SugarAcidityProfilePoint> point() {
    return assessment == null
        ? Optional.empty()
        : Optional.of(assessment.point());
  }
}
