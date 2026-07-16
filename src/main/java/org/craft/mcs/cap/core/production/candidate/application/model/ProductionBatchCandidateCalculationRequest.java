package org.craft.mcs.cap.core.production.candidate.application.model;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Calculation settings accepted for an authoritative production candidate assessment. */
public record ProductionBatchCandidateCalculationRequest(
    @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetObjectiveWeights targetWeights,
    @NonNull ProfileTargetScoringPurpose scoringPurpose,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode
) {

  public ProductionBatchCandidateCalculationRequest {
    requireNonNull(tanninPolicy, "production candidate tannin policy cannot be null");
    requireNonNull(targetWeights, "production candidate target weights cannot be null");
    requireNonNull(scoringPurpose, "production candidate scoring purpose cannot be null");
    requireNonNull(calculatorCode, "production candidate calculator code cannot be null");
    requireNonNull(normalizerCode, "production candidate normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "production candidate objective function code cannot be null");
  }
}
