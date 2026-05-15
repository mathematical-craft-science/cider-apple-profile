package org.craft.mcs.cap.core.production.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CreateProductionBatchCommand(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull ProductionBatchName name,
    @NonNull ProductionBatchKind kind,
    @NonNull List<@NonNull CreateProductionBatchSourceCommand> sources,
    @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetObjectiveWeights targetWeights,
    @NonNull ProfileTargetScoringPurpose scoringPurpose,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode,
    @Nullable String note
) {

  public CreateProductionBatchCommand {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(name, "production batch name cannot be null");
    requireNonNull(kind, "production batch kind cannot be null");
    sources = List.copyOf(requireNonNull(sources, "production batch sources cannot be null"));
    requireNonNull(tanninPolicy, "chemical profile composition tannin policy cannot be null");
    requireNonNull(targetWeights, "profile target objective weights cannot be null");
    requireNonNull(scoringPurpose, "profile target scoring purpose cannot be null");
    requireNonNull(calculatorCode, "chemical profile composition calculator code cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "profile target objective function code cannot be null");

    if (sources.isEmpty()) {
      throw new IllegalArgumentException("production batch must contain at least one source");
    }
    if (sources.stream().map(CreateProductionBatchSourceCommand::feedstockBatchId).distinct().count()
        != sources.size()) {
      throw new IllegalArgumentException("production batch cannot contain duplicate feedstock batch sources");
    }
  }
}
