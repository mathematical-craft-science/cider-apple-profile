package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record TanninTargetRangeRequest(
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public TanninTargetRangeRequest {
    requireNonNull(min, "target tannin minimum cannot be null");
    requireNonNull(max, "target tannin maximum cannot be null");
  }
}
