package org.craft.mcs.cap.core.cideranalysis.classification.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleCharacteristics;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleClassificationSystem;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleClassificationView;
import org.jspecify.annotations.NonNull;


/** Classifies canonical characteristics with a selected cider-apple system. */
public interface ClassifyCiderAppleUseCase {

  @NonNull
  CiderAppleClassificationView classify(
      @NonNull CiderAppleCharacteristics characteristics,
      @NonNull CiderAppleClassificationSystem system
  );
}
