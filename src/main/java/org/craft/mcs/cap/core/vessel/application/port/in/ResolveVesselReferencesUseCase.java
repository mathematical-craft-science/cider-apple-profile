package org.craft.mcs.cap.core.vessel.application.port.in;

import java.util.Map;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.jspecify.annotations.NonNull;


/** Resolves stable vessel identities to display references without exposing the aggregate. */
public interface ResolveVesselReferencesUseCase {

  @NonNull Map<@NonNull VesselId, @NonNull VesselReferenceView> resolve(
      @NonNull ResolveVesselReferencesQuery query
  );
}
