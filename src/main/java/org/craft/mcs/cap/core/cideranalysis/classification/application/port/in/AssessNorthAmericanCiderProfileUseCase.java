package org.craft.mcs.cap.core.cideranalysis.classification.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleCharacteristics;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.NorthAmericanCiderProfileView;
import org.jspecify.annotations.NonNull;


/** Interprets available canonical characteristics with the North American profile. */
public interface AssessNorthAmericanCiderProfileUseCase {

  @NonNull
  NorthAmericanCiderProfileView assess(@NonNull CiderAppleCharacteristics characteristics);
}
