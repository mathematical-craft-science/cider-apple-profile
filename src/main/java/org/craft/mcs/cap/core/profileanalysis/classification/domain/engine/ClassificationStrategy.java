package org.craft.mcs.cap.core.profileanalysis.classification.domain.engine;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationProfile;
import org.jspecify.annotations.NonNull;


/**
 * Domain strategy that exposes one cider apple classification profile.
 *
 * <p>Implementations are small adapters from a named classification tradition to a
 * {@link ClassificationProfile}: they provide the public code and the ordered rules used by
 * {@link CiderAppleClassifier}. They do not load apple varieties, decide API behavior or
 * handle incomplete profiles; those concerns belong to scenario-specific application input checks.
 *
 * @param <C> enum type of classes produced by the profile
 */
public interface ClassificationStrategy<C extends Enum<C>> {

  @NonNull
  String code();

  @NonNull
  Set<@NonNull ClassificationCharacteristic> requiredCharacteristics();

  default @NonNull ClassificationIntervalPolicy intervalPolicy() {
    return ClassificationIntervalPolicy.MIDPOINT_WITH_BOUNDARY_EVIDENCE;
  }

  @NonNull
  ClassificationProfile<C> profile();
}
