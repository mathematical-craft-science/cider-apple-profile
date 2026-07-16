package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Typed explanation facts for a target-aware source role in feedstock planning.
 *
 * <p>This read model intentionally avoids presentation text. It gives inbound adapters stable facts
 * from which localized explanations can be derived.
 */
public record FeedstockPlanningRoleExplanationView(
    @NonNull List<@NonNull FeedstockPlanningRoleEvidenceFact> evidence
) {

  public FeedstockPlanningRoleExplanationView {
    evidence = List.copyOf(requireNonNull(evidence, "feedstock planning role explanation evidence cannot be null"));
  }
}
