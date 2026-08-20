package org.craft.mcs.cap.core.appleprofile.catalog.application.contract;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable keyset position within the ordered apple-variety catalog. */
public record AppleVarietyCatalogPagePosition(
    @NonNull String name,
    @NonNull UUID varietyId
) {

  public AppleVarietyCatalogPagePosition {
    requireNonNull(name, "apple variety page position name cannot be null");
    requireNonNull(varietyId, "apple variety page position id cannot be null");
  }
}
