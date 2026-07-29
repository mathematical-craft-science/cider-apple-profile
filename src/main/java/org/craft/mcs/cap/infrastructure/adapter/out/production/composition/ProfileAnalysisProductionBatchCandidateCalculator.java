package org.craft.mcs.cap.infrastructure.adapter.out.production.composition;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.candidate.application.model.AvailableProductionSource;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateCalculator;
import org.craft.mcs.cap.core.production.application.model.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CalculatedComposition;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceCharacteristics;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Adapts authoritative production source snapshots to the generic profile-analysis calculator. */
@Component
@RequiredArgsConstructor
public final class ProfileAnalysisProductionBatchCandidateCalculator
    implements ProductionBatchCandidateCalculator {

  private final @NonNull CalculateCompositionUseCase calculateCompositionUseCase;

  @Override
  public @NonNull ProductionBatchCandidateCalculation calculate(
      @NonNull List<@NonNull AvailableProductionSource> sources,
      @NonNull ProductionBatchCandidateCalculationRequest request
  ) {
    requireNonNull(sources, "available production sources cannot be null");
    requireNonNull(request, "production batch candidate calculation request cannot be null");
    final var result = calculateCompositionUseCase.calculate(new CalculateCompositionCommand(
        sources.stream()
            .map(ProfileAnalysisProductionBatchCandidateCalculator::toCompositionSource)
            .toList(),
        request.tanninPolicy(),
        request.targetObjective(),
        request.scoringConfiguration(),
        request.calculatorCode()
    ));
    return new ProductionBatchCandidateCalculation(
        toProductionProfile(result.composition()),
        result.potentialAbv(),
        result.normalizerCode(),
        result.targetDistance(),
        result.composition().warnings()
    );
  }

  private static @NonNull CompositionSource toCompositionSource(
      @NonNull AvailableProductionSource source
  ) {
    return new CompositionSource(
        new CompositionSourceReference(
            source.reference().type().name(),
            source.reference().id().toString()
        ),
        toCompositionCharacteristics(source.characteristics()),
        new AbsoluteCompositionVolume(source.selectedVolume()),
        source.availableVolume()
    );
  }

  private static @NonNull CompositionSourceCharacteristics toCompositionCharacteristics(
      @NonNull ProductionSourceCharacteristics characteristics
  ) {
    return new CompositionSourceCharacteristics(
        characteristics.specificGravity(),
        characteristics.titratableAcidity(),
        characteristics.tannin(),
        characteristics.yeastAssimilableNitrogen()
    );
  }

  private static @NonNull ProductionProfileSnapshot toProductionProfile(
      @NonNull CalculatedComposition composition
  ) {
    final var characteristics = composition.characteristics();
    return new ProductionProfileSnapshot(
        characteristics.specificGravity(),
        characteristics.titratableAcidity(),
        characteristics.tannin(),
        characteristics.yeastAssimilableNitrogen()
    );
  }
}
