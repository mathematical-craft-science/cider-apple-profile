package org.craft.mcs.cap.core.measurement.application.port.out;

import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.jspecify.annotations.NonNull;


/** Provider-neutral source of the actor reference recorded with immutable revisions. */
public interface MeasurementActorProvider {

  @NonNull ActorReference requireCurrentActorReference();
}
