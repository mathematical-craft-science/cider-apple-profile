package org.craft.mcs.cap.shared.domain.classification.engine;

import org.craft.mcs.cap.shared.domain.classification.rule.ClassificationProfile;
import org.jspecify.annotations.NonNull;


public interface ClassificationStrategy<C extends Enum<C>> {

  @NonNull
  String code();

  @NonNull
  ClassificationProfile<C> profile();
}
