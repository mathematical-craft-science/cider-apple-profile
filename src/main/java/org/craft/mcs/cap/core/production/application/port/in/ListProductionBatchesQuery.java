package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ListProductionBatchesQuery(@NonNull CiderMakerId ciderMakerId) {

  public ListProductionBatchesQuery {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
  }
}
