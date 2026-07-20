package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable keyset position for the apple-variety sugar-acidity catalog query. */
public record AppleVarietySugarAcidityPagePosition(
    @NonNull String name,
    @NonNull UUID varietyId
) {

  public AppleVarietySugarAcidityPagePosition {
    requireNonNull(name, "apple variety page position name cannot be null");
    requireNonNull(varietyId, "apple variety page position id cannot be null");
  }
}
