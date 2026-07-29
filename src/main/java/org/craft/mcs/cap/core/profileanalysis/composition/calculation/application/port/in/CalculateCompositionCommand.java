package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CalculateCompositionCommand(
    @NonNull List<@NonNull CompositionSource> sources,
    @NonNull CompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetScoringConfiguration scoringConfiguration,
    @NonNull CompositionCalculatorCode calculatorCode
) {

  public CalculateCompositionCommand {
    sources = List.copyOf(requireNonNull(sources, "composition sources cannot be null"));
    requireNonNull(tanninPolicy, "composition tannin policy cannot be null");
    requireNonNull(scoringConfiguration, "profile target scoring configuration cannot be null");
    requireNonNull(calculatorCode, "composition calculator code cannot be null");
  }

}
