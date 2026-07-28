package org.craft.mcs.cap.core.profileanalysis.classification.domain.result;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record BorderlineEvidence(
    @NonNull ClassificationCharacteristic characteristic,
    @NonNull BorderlineReason reason
) {

  public BorderlineEvidence {
    requireNonNull(characteristic, "classification characteristic cannot be null");
    requireNonNull(reason, "borderline reason cannot be null");
  }
}
