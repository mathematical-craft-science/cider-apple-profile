package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper;

import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileEvidenceView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockProfileEvidenceResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps per-characteristic current-profile evidence for Feedstock read models. */
public final class FeedstockProfileEvidenceRestMapper {

  private FeedstockProfileEvidenceRestMapper() {
  }

  public static @NonNull FeedstockProfileEvidenceResponse toResponse(
      @NonNull FeedstockCurrentProfileEvidenceView evidence
  ) {
    requireNonNull(evidence, "feedstock profile evidence cannot be null");
    return new FeedstockProfileEvidenceResponse(
        evidence.characteristic(),
        evidence.measurementSetId(),
        evidence.recordId(),
        evidence.measuredAt(),
        evidence.source(),
        evidence.confidence(),
        evidence.freshnessStatus(),
        evidence.stale()
    );
  }
}
