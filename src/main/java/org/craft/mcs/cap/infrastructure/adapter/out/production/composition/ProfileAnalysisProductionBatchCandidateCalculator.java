package org.craft.mcs.cap.infrastructure.adapter.out.production.composition;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.candidate.application.model.AvailableProductionSource;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateCalculator;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileComposition;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Adapts authoritative production source snapshots to the generic profile-analysis calculator. */
@Component
@RequiredArgsConstructor
public final class ProfileAnalysisProductionBatchCandidateCalculator
    implements ProductionBatchCandidateCalculator {

  private final @NonNull CalculateChemicalProfileCompositionUseCase compositionCalculator;

  @Override
  public @NonNull ProductionBatchCandidateCalculation calculate(
      @NonNull List<@NonNull AvailableProductionSource> sources,
      @NonNull ProductionBatchCandidateCalculationRequest request
  ) {
    requireNonNull(sources, "available production sources cannot be null");
    requireNonNull(request, "production batch candidate calculation request cannot be null");
    final var result = compositionCalculator.calculate(new CalculateChemicalProfileCompositionCommand(
        sources.stream()
            .map(ProfileAnalysisProductionBatchCandidateCalculator::toCompositionSource)
            .toList(),
        request.tanninPolicy(),
        request.targetObjective(),
        request.targetWeights(),
        request.scoringPurpose(),
        request.calculatorCode(),
        request.normalizerCode(),
        request.objectiveFunctionCode()
    ));
    return new ProductionBatchCandidateCalculation(
        toProductionProfile(result.composition()),
        result.potentialAbv(),
        result.normalizerCode(),
        result.targetDistance(),
        result.composition().warnings()
    );
  }

  private static @NonNull ChemicalProfileCompositionSource toCompositionSource(
      @NonNull AvailableProductionSource source
  ) {
    return new ChemicalProfileCompositionSource(
        new ChemicalProfileCompositionSourceReference(
            source.reference().type().name(),
            source.reference().id().toString()
        ),
        source.profile(),
        new AbsoluteCompositionVolume(source.selectedVolume()),
        source.availableVolume()
    );
  }

  private static @NonNull ProductionProfileSnapshot toProductionProfile(
      @NonNull ChemicalProfileComposition composition
  ) {
    final var profile = composition.profile();
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of(profile.sugar().value()),
        TitratableAcidityValue.of(profile.acidity().value()),
        toTannin(profile.tannin()),
        toYeastAssimilableNitrogen(composition.yeastAssimilableNitrogen())
    );
  }

  private static @Nullable TanninValue toTannin(@Nullable SugarAcidityAxisPoint tannin) {
    return tannin == null ? null : TanninValue.of(tannin.value());
  }

  private static @Nullable YeastAssimilableNitrogenValue toYeastAssimilableNitrogen(
      @Nullable SugarAcidityAxisPoint yeastAssimilableNitrogen
  ) {
    return yeastAssimilableNitrogen == null
        ? null
        : YeastAssimilableNitrogenValue.of(yeastAssimilableNitrogen.value());
  }
}
