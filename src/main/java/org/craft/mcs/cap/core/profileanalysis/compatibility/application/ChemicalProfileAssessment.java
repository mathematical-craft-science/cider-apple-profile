package org.craft.mcs.cap.core.profileanalysis.compatibility.application;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Transitional assessment assembled for callers that still provide a broad ChemicalProfile.
 *
 * <p>The model keeps the graph result separate from the optional North American tannin category
 * used for current graph-point styling.
 */
public record ChemicalProfileAssessment(
    @NonNull ChemicalProfileAxes axes,
    @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
    @Nullable NorthAmericanTanninLevel northAmericanTanninLevel,
    @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
) {

  public ChemicalProfileAssessment {
    requireNonNull(axes, "chemical profile axes cannot be null");
    requireNonNull(graphEvaluation, "sugar-acidity graph evaluation cannot be null");
    requireNonNull(capabilityAssessment, "chemical profile capability assessment cannot be null");
    if (capabilityAssessment.capability() != ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH) {
      throw new DomainRuleViolationException(
          "chemical profile graph assessment requires sugar-acidity graph capability"
      );
    }
    validateCapabilityEvidence(axes, capabilityAssessment);
    validateGraphEvidence(axes, graphEvaluation);
    if (capabilityAssessment.capable() != graphEvaluation.plottable()) {
      throw new DomainRuleViolationException(
          "chemical profile capability and graph projection must agree"
      );
    }
    if (northAmericanTanninLevel != null && !graphEvaluation.plottable()) {
      throw new DomainRuleViolationException(
          "North American tannin styling requires a plottable graph evaluation"
      );
    }
  }

  public boolean plottable() {
    return graphEvaluation.plottable();
  }

  public @Nullable JolicoeurSugarAcidityZoneAssessment assessment() {
    return graphEvaluation.zoneAssessment();
  }

  private static void validateCapabilityEvidence(
      @NonNull ChemicalProfileAxes axes,
      @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
  ) {
    for (final var requiredAxis : capabilityAssessment.requiredAxes()) {
      if (axes.has(requiredAxis) != capabilityAssessment.presentAxes().contains(requiredAxis)) {
        throw new DomainRuleViolationException(
            "chemical profile axes and capability evidence must agree for " + requiredAxis
        );
      }
    }
  }

  private static void validateGraphEvidence(
      @NonNull ChemicalProfileAxes axes,
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation
  ) {
    validateGraphCharacteristic(
        axes.has(ChemicalAxis.SPECIFIC_GRAVITY),
        graphEvaluation.missingCharacteristics().contains(GraphMissingCharacteristic.SPECIFIC_GRAVITY),
        ChemicalAxis.SPECIFIC_GRAVITY
    );
    validateGraphCharacteristic(
        axes.has(ChemicalAxis.TITRATABLE_ACIDITY),
        graphEvaluation.missingCharacteristics().contains(GraphMissingCharacteristic.TITRATABLE_ACIDITY),
        ChemicalAxis.TITRATABLE_ACIDITY
    );
  }

  private static void validateGraphCharacteristic(
      boolean present,
      boolean missing,
      @NonNull ChemicalAxis axis
  ) {
    if (present == missing) {
      throw new DomainRuleViolationException(
          "chemical profile axes and graph evidence must agree for " + axis
      );
    }
  }

}
