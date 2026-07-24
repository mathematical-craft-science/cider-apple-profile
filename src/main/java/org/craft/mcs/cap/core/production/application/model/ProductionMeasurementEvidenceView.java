package org.craft.mcs.cap.core.production.application.model;

import java.time.Instant;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Production-owned projection of evidence selected for one current-profile characteristic. */
public record ProductionMeasurementEvidenceView(
    @NonNull Characteristic characteristic,
    @NonNull ProductionMeasurementEvidenceReference evidenceReference,
    @NonNull Instant measuredAt,
    @NonNull ProductionMeasurementProvenanceView provenance,
    @NonNull FreshnessStatus freshnessStatus
) {

  public ProductionMeasurementEvidenceView {
    requireNonNull(characteristic, "production measurement characteristic cannot be null");
    requireNonNull(evidenceReference, "production measurement evidence reference cannot be null");
    requireNonNull(measuredAt, "production measurement time cannot be null");
    requireNonNull(provenance, "production measurement provenance cannot be null");
    requireNonNull(freshnessStatus, "production measurement freshness cannot be null");
  }

  /** Production-owned chemical characteristics accepted from the Measurement context. */
  public enum Characteristic {

    /** Specific gravity used as the sugar axis. */
    SUGAR,

    /** Acidity activity expressed on the pH scale. */
    PH,

    /** Titratable acidity normalized as malic acid. */
    TITRATABLE_ACIDITY,

    /** Tannin concentration normalized as tannic-acid equivalents. */
    TANNIN,

    /** Yeast-assimilable nitrogen concentration. */
    YAN
  }

  /** Freshness evaluated against the owning workspace policy. */
  public enum FreshnessStatus {

    /** The record remains within the configured freshness period. */
    CURRENT,

    /** The record reached or exceeded the configured stale threshold. */
    STALE
  }
}
