package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveWeights;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CalculateChemicalProfileCompositionCommand(
    @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
    @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetObjectiveWeights targetWeights,
    @NonNull ProfileTargetScoringPurpose scoringPurpose
) {

  public CalculateChemicalProfileCompositionCommand {
    sources = List.copyOf(requireNonNull(sources, "chemical profile composition sources cannot be null"));
    requireNonNull(tanninPolicy, "chemical profile composition tannin policy cannot be null");
    requireNonNull(targetWeights, "profile target objective weights cannot be null");
    requireNonNull(scoringPurpose, "profile target scoring purpose cannot be null");
  }

  public CalculateChemicalProfileCompositionCommand(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy
  ) {
    this(
        sources,
        tanninPolicy,
        null,
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetScoringPurpose.BLENDING_PLANNING
    );
  }
}
