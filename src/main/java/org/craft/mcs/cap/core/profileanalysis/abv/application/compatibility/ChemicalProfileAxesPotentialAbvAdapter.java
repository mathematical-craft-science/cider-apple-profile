package org.craft.mcs.cap.core.profileanalysis.abv.application.compatibility;

import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Adapts transitional profile axes to the Specific Gravity input required by potential ABV prediction. */
public final class ChemicalProfileAxesPotentialAbvAdapter {

  private ChemicalProfileAxesPotentialAbvAdapter() {
  }

  public static @NonNull Optional<@NonNull PotentialAbvPrediction> predict(
      @NonNull ChemicalProfileAxes axes,
      @NonNull PotentialAbvPredictor predictor
  ) {
    requireNonNull(axes, "chemical profile axes cannot be null");
    requireNonNull(predictor, "potential ABV predictor cannot be null");
    return axes.valueOf(ChemicalAxis.SPECIFIC_GRAVITY)
        .map(axis -> predictor.predict(SpecificGravityValue.of(axis.value())));
  }
}
