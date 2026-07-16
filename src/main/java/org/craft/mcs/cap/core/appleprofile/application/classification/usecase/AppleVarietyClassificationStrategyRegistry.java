package org.craft.mcs.cap.core.appleprofile.application.classification.usecase;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import java.util.Map;
import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.application.classification.port.in.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.french.FrenchClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.longashton.LongAshton4ClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.longashton.LongAshton7ClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.spanish.SpanishClassificationStrategy;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Registry that maps public apple-variety classification systems to domain strategies.
 *
 * <p>This is an application-level catalog of built-in cider classification systems used only for
 * apple variety classification. North American profile calculation is intentionally not registered
 * here because it is a broader profile projection that can be reused outside the generic
 * classification endpoint.
 */
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

  @SuppressWarnings("unchecked")
  public <C extends Enum<C>> @NonNull ClassificationStrategy<C> get(
      @NonNull AppleVarietyClassificationSystem system
  ) {
    requireNonNull(system, "apple variety classification system cannot be null");

    return (ClassificationStrategy<C>) Optional.ofNullable(strategies.get(system))
        .orElseThrow(() -> new ApplicationValidationException(
            "unsupported apple variety classification system: " + system
        ));
  }
}
