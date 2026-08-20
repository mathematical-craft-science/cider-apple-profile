package org.craft.mcs.cap.core.cideranalysis.classification.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleCharacteristics;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.NorthAmericanCiderProfileView;
import org.craft.mcs.cap.core.cideranalysis.classification.application.port.in.AssessNorthAmericanCiderProfileUseCase;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Application boundary for independent North American sugar, acidity, and tannin interpretation. */
@Component
@RequiredArgsConstructor
public final class AssessNorthAmericanCiderProfileService implements AssessNorthAmericanCiderProfileUseCase {

  private final @NonNull NorthAmericanProfileCalculator calculator;

  @Override
  public @NonNull NorthAmericanCiderProfileView assess(
      @NonNull CiderAppleCharacteristics characteristics
  ) {
    requireNonNull(characteristics, "cider-apple characteristics cannot be null");
    final var specificGravity = characteristics.specificGravity();
    final var titratableAcidity = characteristics.titratableAcidity();
    final var tannin = characteristics.tannin();
    return new NorthAmericanCiderProfileView(
        specificGravity == null
            ? null
            : NorthAmericanCiderProfileView.SugarLevel.valueOf(calculator.assessSugar(specificGravity).name()),
        titratableAcidity == null
            ? null
            : NorthAmericanCiderProfileView.AcidityLevel.valueOf(
                calculator.assessAcidity(titratableAcidity).name()
            ),
        tannin == null
            ? null
            : NorthAmericanCiderProfileView.TanninLevel.valueOf(calculator.assessTannin(tannin).name())
    );
  }
}
