package org.craft.mcs.cap.core.formulation.calculation.application.support;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.formulation.calculation.domain.CalculatedComposition;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSource;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Internal Formulation mapper from calculation domain results to the published immutable view. */
public final class CalculatedCompositionViewMapper {

  private CalculatedCompositionViewMapper() {
  }

  public static @NonNull CalculatedCompositionView toView(@NonNull CalculatedComposition composition) {
    requireNonNull(composition, "calculated composition cannot be null");
    final var characteristics = composition.characteristics();
    return new CalculatedCompositionView(
        composition.sources().stream().map(CalculatedCompositionViewMapper::toView).toList(),
        new CalculatedCompositionView.Characteristics(
            characteristics.specificGravity(),
            characteristics.titratableAcidity(),
            characteristics.tannin(),
            characteristics.yeastAssimilableNitrogen()
        ),
        composition.totalVolume(),
        composition.warnings().stream()
            .map(warning -> CalculatedCompositionView.Warning.valueOf(warning.name()))
            .toList()
    );
  }

  private static @NonNull CompositionSourceInput toView(@NonNull CompositionSource source) {
    final var characteristics = source.characteristics();
    return new CompositionSourceInput(
        new CompositionSourceInput.Reference(source.reference().type(), source.reference().id()),
        new CompositionSourceInput.Characteristics(
            characteristics.specificGravity(),
            characteristics.titratableAcidity(),
            characteristics.tannin(),
            characteristics.yeastAssimilableNitrogen()
        ),
        switch (source.weight()) {
          case AbsoluteCompositionVolume(var volume) -> new CompositionSourceInput.AbsoluteVolume(volume);
          case CompositionProportion(var value) -> new CompositionSourceInput.Proportion(value);
        },
        source.availableVolume()
    );
  }
}
