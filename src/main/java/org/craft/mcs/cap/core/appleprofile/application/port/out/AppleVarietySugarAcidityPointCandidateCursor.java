package org.craft.mcs.cap.core.appleprofile.application.port.out;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietySugarAcidityPointCandidateCursor(
    @NonNull String name,
    @NonNull UUID varietyId
) {

  public AppleVarietySugarAcidityPointCandidateCursor {
    requireNonNull(name, "apple variety point candidate cursor name cannot be null");
    requireNonNull(varietyId, "apple variety point candidate cursor id cannot be null");
  }
}
