package org.craft.mcs.cap.core.cideranalysis.classification.engine;

import org.craft.mcs.cap.core.cideranalysis.classification.rule.ClassificationProfile;
import org.jspecify.annotations.NonNull;


public interface ClassificationStrategy<C extends Enum<C>> {

  @NonNull
  String code();

  @NonNull
  ClassificationProfile<C> profile();
}
