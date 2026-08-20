package org.craft.mcs.cap.core.formulation.calculation.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.formulation.calculation.application.InvalidCompositionRequestException;
import org.craft.mcs.cap.core.formulation.calculation.application.UnsupportedCompositionCalculatorRequestException;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput.Weight;
import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionUseCase;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationAbvAnalyzer;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationTargetFitAnalyzer;
import org.craft.mcs.cap.core.formulation.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculationRequest;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculatorSelector;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceWeight;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.domain.InvalidCompositionException;
import org.craft.mcs.cap.core.formulation.calculation.domain.UnsupportedCompositionCalculatorException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.formulation.calculation.application.support.CalculatedCompositionViewMapper.toView;


/** Calculates mass balance, then enriches the immutable result through outbound analysis ports. */
@Component
@RequiredArgsConstructor
public final class CalculateCompositionService implements CalculateCompositionUseCase {

  private final @NonNull CompositionCalculatorSelector calculatorSelector;
  private final @NonNull FormulationAbvAnalyzer abvAnalyzer;
  private final @NonNull FormulationTargetFitAnalyzer targetFitAnalyzer;

  @Override
  public @NonNull CalculateCompositionResult calculate(
      @NonNull CalculateCompositionCommand command
  ) {
    requireNonNull(command, "calculate composition command cannot be null");
    try {
      return calculateValidated(command);
    } catch (InvalidCompositionException exception) {
      throw new InvalidCompositionRequestException(exception.getMessage(), exception);
    } catch (UnsupportedCompositionCalculatorException exception) {
      throw new UnsupportedCompositionCalculatorRequestException(exception.getMessage(), exception);
    }
  }

  private @NonNull CalculateCompositionResult calculateValidated(
      @NonNull CalculateCompositionCommand command
  ) {
    final var composition = calculatorSelector.select(CompositionCalculatorCode.valueOf(command.calculator().name()))
        .calculate(new CompositionCalculationRequest(
            command.sources().stream().map(CalculateCompositionService::toDomain).toList(),
            CompositionTanninPolicy.valueOf(command.tanninPolicy().name())
        ));
    final var view = toView(composition);
    final var potentialAbv = abvAnalyzer.predict(view.characteristics().specificGravity());
    final var target = command.target();
    if (target == null) {
      return new CalculateCompositionResult(view, potentialAbv, null, null);
    }
    return new CalculateCompositionResult(
        view,
        potentialAbv,
        target.scoring().normalizer(),
        targetFitAnalyzer.evaluate(view.characteristics(), target)
    );
  }

  private static @NonNull CompositionSource toDomain(@NonNull CompositionSourceInput source) {
    return new CompositionSource(
        new CompositionSourceReference(source.reference().type(), source.reference().id()),
        new CompositionSourceCharacteristics(
            source.characteristics().specificGravity(),
            source.characteristics().titratableAcidity(),
            source.characteristics().tannin(),
            source.characteristics().yeastAssimilableNitrogen()
        ),
        toDomain(source.weight()),
        source.availableVolume()
    );
  }

  private static @NonNull CompositionSourceWeight toDomain(
      @NonNull Weight weight
  ) {
    return switch (weight) {
      case CompositionSourceInput.AbsoluteVolume(var volume) -> new AbsoluteCompositionVolume(volume);
      case CompositionSourceInput.Proportion(var value) -> new CompositionProportion(value);
    };
  }

}
