package org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.in;

import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanClassification;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


public interface ClassifyAppleVarietyNorthAmericanUseCase {

  @NonNull
  AppleVarietyNorthAmericanClassification classify(
      @NonNull AppleVarietyId varietyId
  );
}
