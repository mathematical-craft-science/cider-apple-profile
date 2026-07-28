package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CalculateChemicalProfileCompositionCommand(
    @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
    @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetScoringConfiguration scoringConfiguration,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode
) {

  public CalculateChemicalProfileCompositionCommand {
    sources = List.copyOf(requireNonNull(sources, "chemical profile composition sources cannot be null"));
    requireNonNull(tanninPolicy, "chemical profile composition tannin policy cannot be null");
    requireNonNull(scoringConfiguration, "profile target scoring configuration cannot be null");
    requireNonNull(calculatorCode, "chemical profile composition calculator code cannot be null");
  }

}
