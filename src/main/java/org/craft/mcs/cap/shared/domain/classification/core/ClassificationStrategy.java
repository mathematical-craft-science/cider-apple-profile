package org.craft.mcs.cap.shared.domain.classification.core;

import org.jspecify.annotations.NonNull;


public interface ClassificationStrategy<C extends Enum<C>> {

  @NonNull
  String code();

  @NonNull
  ClassificationProfile<C> profile();
}
