package org.craft.mcs.cap.core.appleprofile.application.usecase;

import java.util.Map;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationStrategy;
import org.craft.mcs.cap.shared.domain.classification.profile.english.EnglishClassificationStrategy;
import org.craft.mcs.cap.shared.domain.classification.profile.french.FrenchClassificationStrategy;
import org.craft.mcs.cap.shared.domain.classification.profile.longashton.LongAshton4ClassificationStrategy;
import org.craft.mcs.cap.shared.domain.classification.profile.longashton.LongAshton7ClassificationStrategy;
import org.craft.mcs.cap.shared.domain.classification.profile.spanish.SpanishClassificationStrategy;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


@Component
public class AppleVarietyClassificationStrategyRegistry {

  private final @NonNull Map<AppleVarietyClassificationSystem, ClassificationStrategy<?>> strategies =
      Map.of(
          AppleVarietyClassificationSystem.ENGLISH, new EnglishClassificationStrategy(),
          AppleVarietyClassificationSystem.FRENCH, new FrenchClassificationStrategy(),
          AppleVarietyClassificationSystem.SPANISH, new SpanishClassificationStrategy(),
          AppleVarietyClassificationSystem.LONG_ASHTON_4, new LongAshton4ClassificationStrategy(),
          AppleVarietyClassificationSystem.LONG_ASHTON_7, new LongAshton7ClassificationStrategy()
      );

  @NonNull
  ClassificationStrategy<?> get(@NonNull AppleVarietyClassificationSystem system) {
    final var strategy = strategies.get(system);

    if (strategy == null) {
      throw new IllegalArgumentException("unsupported apple variety classification system: " + system);
    }

    return strategy;
  }
}
