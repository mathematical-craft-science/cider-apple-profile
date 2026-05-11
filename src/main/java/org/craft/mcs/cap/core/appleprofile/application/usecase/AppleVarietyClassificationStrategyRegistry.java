package org.craft.mcs.cap.core.appleprofile.application.usecase;

import java.util.Map;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.cideranalysis.classification.engine.ClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.french.FrenchClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.longashton.LongAshton4ClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.longashton.LongAshton7ClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.spanish.SpanishClassificationStrategy;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
public class AppleVarietyClassificationStrategyRegistry {


  private final @NonNull Map<@NonNull AppleVarietyClassificationSystem, @NonNull ClassificationStrategy<?>> strategies;

  public AppleVarietyClassificationStrategyRegistry() {
    this.strategies = Map.of(

        AppleVarietyClassificationSystem.FRENCH,
        new FrenchClassificationStrategy(BuiltInCiderClassificationThresholds.french()),

        AppleVarietyClassificationSystem.SPANISH,
        new SpanishClassificationStrategy(BuiltInCiderClassificationThresholds.spanish()),

        AppleVarietyClassificationSystem.LONG_ASHTON_4,
        new LongAshton4ClassificationStrategy(BuiltInCiderClassificationThresholds.longAshton4()),

        AppleVarietyClassificationSystem.LONG_ASHTON_7,
        new LongAshton7ClassificationStrategy(BuiltInCiderClassificationThresholds.longAshton7())
    );
  }

  public @NonNull ClassificationStrategy<?> get(
      @NonNull AppleVarietyClassificationSystem system
  ) {
    requireNonNull(system, "apple variety classification system cannot be null");

    final var strategy = strategies.get(system);

    if (strategy == null) { // NOSONAR
      throw new IllegalArgumentException("unsupported apple variety classification system: " + system);
    }

    return strategy;
  }
}
