package org.craft.mcs.cap.core.production.candidate.application.model;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Calculation settings accepted for an authoritative production candidate assessment. */
public record ProductionBatchCandidateCalculationRequest(
    @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetScoringConfiguration scoringConfiguration,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode
) {

  public ProductionBatchCandidateCalculationRequest {
    requireNonNull(tanninPolicy, "production candidate tannin policy cannot be null");
    requireNonNull(scoringConfiguration, "production candidate scoring configuration cannot be null");
    requireNonNull(calculatorCode, "production candidate calculator code cannot be null");
  }
}
