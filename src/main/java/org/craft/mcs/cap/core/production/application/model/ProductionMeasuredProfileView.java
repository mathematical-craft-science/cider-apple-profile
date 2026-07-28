package org.craft.mcs.cap.core.production.application.model;

import java.util.List;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Current Production characteristics backed specifically by measurement evidence.
 *
 * <p>This projection does not include values inherited from the accepted initial production
 * snapshot. {@link ProductionCurrentProfileView} composes both sources into the effective current
 * profile.</p>
 */
public record ProductionMeasuredProfileView(
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull List<@NonNull MeasurementEvidenceView> evidence
) {

  public ProductionMeasuredProfileView {
    requireNonNull(chemicalProfile, "production measured chemical profile cannot be null");
    evidence = List.copyOf(requireNonNull(evidence, "production measurement evidence cannot be null"));
  }
}
