package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record BorderlineEvidenceResponse(
    @NonNull String measure,
    @NonNull String borderlineCondition
) {

  public BorderlineEvidenceResponse {
    requireNonNull(measure, "measure cannot be null");
    requireNonNull(borderlineCondition, "borderlineCondition cannot be null");
  }

}
