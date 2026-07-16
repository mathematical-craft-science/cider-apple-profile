package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import java.util.UUID;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Public Feedstock evidence revision exposed to material consumers without leaking measurement domain types. */
public record FeedstockBatchMaterialEvidenceReference(
    @NonNull UUID measurementId,
    long version
) {

  public FeedstockBatchMaterialEvidenceReference {
    requireNonNull(measurementId, "feedstock measurement evidence id cannot be null");
    if (version < 0) {
      throw new ApplicationValidationException("feedstock measurement evidence version cannot be negative");
    }
  }

  public @NonNull String externalValue() {
    return measurementId + ":v" + version;
  }
}
