package org.craft.mcs.cap.core.production.application.model;

import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Production-owned read projection of the provenance associated with immutable Measurement evidence.
 *
 * <p>Optional text values are opaque display metadata already validated by the Measurement context.
 * Production does not duplicate Measurement domain validation or interpret these values.</p>
 */
public record ProductionMeasurementProvenanceView(
    @NonNull SourceType sourceType,
    @Nullable String sourceName,
    @Nullable String method,
    @Nullable String reference,
    @Nullable ConfidenceLevel confidence
) {

  public ProductionMeasurementProvenanceView {
    requireNonNull(sourceType, "production measurement source type cannot be null");
  }

  /** Production-owned provenance categories accepted from the Measurement context. */
  public enum SourceType {

    /** A person entered the value directly. */
    USER,

    /** A registered or identified instrument produced the value. */
    INSTRUMENT,

    /** A laboratory supplied the value. */
    LAB,

    /** An external import supplied the value. */
    IMPORT,

    /** A catalog curator supplied or approved the value. */
    CURATOR,

    /** A research or educational institute supplied the value. */
    INSTITUTE,

    /** CAP derived the result from immutable observations. */
    DERIVED
  }
}
