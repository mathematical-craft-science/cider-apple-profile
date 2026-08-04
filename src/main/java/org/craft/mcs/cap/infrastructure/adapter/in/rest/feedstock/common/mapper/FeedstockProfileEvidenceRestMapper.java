package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockProfileEvidenceResponse;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps per-characteristic current-profile evidence for Feedstock read models. */
public final class FeedstockProfileEvidenceRestMapper {

  private FeedstockProfileEvidenceRestMapper() {
  }

  public static @NonNull FeedstockProfileEvidenceResponse toResponse(
      @NonNull MeasurementEvidenceView evidence
  ) {
    requireNonNull(evidence, "feedstock profile evidence cannot be null");
    final var reference = evidence.evidenceReference();
    final var provenance = evidence.provenance();
    return new FeedstockProfileEvidenceResponse(
        evidence.characteristic(),
        evidence.origin(),
        evidence.materialStateId().value(),
        reference.measurementSetId(),
        reference.recordId(),
        evidence.measuredAt(),
        provenance.sourceName(),
        provenance.confidence(),
        evidence.freshnessStatus(),
        evidence.freshnessStatus() == MeasurementFreshnessStatus.STALE
    );
  }
}
