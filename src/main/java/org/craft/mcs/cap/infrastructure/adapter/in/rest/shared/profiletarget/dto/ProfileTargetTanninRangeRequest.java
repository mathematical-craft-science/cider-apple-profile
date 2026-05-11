package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProfileTargetTanninRangeRequest(
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public ProfileTargetTanninRangeRequest {
    requireNonNull(min, "tannin target minimum cannot be null");
    requireNonNull(max, "tannin target maximum cannot be null");
  }
}
