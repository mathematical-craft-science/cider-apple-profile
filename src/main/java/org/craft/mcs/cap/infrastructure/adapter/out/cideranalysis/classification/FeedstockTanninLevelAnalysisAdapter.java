package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.classification;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleCharacteristics;
import org.craft.mcs.cap.core.cideranalysis.classification.application.port.in.AssessNorthAmericanCiderProfileUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.contract.FeedstockNorthAmericanTanninLevel;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.port.out.FeedstockTanninLevelAnalyzer;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL from Feedstock tannin interpretation to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class FeedstockTanninLevelAnalysisAdapter implements FeedstockTanninLevelAnalyzer {

  private final @NonNull AssessNorthAmericanCiderProfileUseCase useCase;

  @Override
  public @NonNull FeedstockNorthAmericanTanninLevel assess(
      @NonNull CharacteristicValue<TanninValue> tannin
  ) {
    final var result = useCase.assess(new CiderAppleCharacteristics(null, null, tannin));
    return FeedstockNorthAmericanTanninLevel.valueOf(
        requireNonNull(result.tannin(), "North American tannin assessment cannot be null").name()
    );
  }
}
