package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** One state-aware characteristic selected from immutable measurement evidence. */
public record CurrentMeasurementCharacteristicView(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull CharacteristicValue<MeasuredValue> value,
    @NonNull MeasurementValueOrigin origin,
    @NonNull MaterialStateId evidenceMaterialStateId,
    @NonNull MeasurementSetId measurementSetId,
    @NonNull MeasurementRecordId recordId,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance,
    @NonNull MeasurementFreshnessStatus freshnessStatus
) {

  public CurrentMeasurementCharacteristicView {
    requireNonNull(characteristic, "current measurement characteristic cannot be null");
    requireNonNull(value, "current measurement value cannot be null");
    requireNonNull(origin, "current measurement value origin cannot be null");
    requireNonNull(evidenceMaterialStateId, "evidence material state id cannot be null");
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(measuredAt, "measurement time cannot be null");
    requireNonNull(provenance, "measurement provenance cannot be null");
    requireNonNull(freshnessStatus, "measurement freshness status cannot be null");
    if (!characteristic.supports(value)) {
      throw new ApplicationValidationException(
          "measurement characteristic " + characteristic + " has an incompatible value"
      );
    }
  }

  /** Publishes the selected characteristic's evidence without changing its measured value. */
  public @NonNull MeasurementEvidenceView evidence() {
    return new MeasurementEvidenceView(
        new MeasurementEvidenceSnapshot(
            characteristic,
            origin,
            evidenceMaterialStateId,
            new MeasurementEvidenceReference(measurementSetId.value(), recordId.value()),
            measuredAt,
            new MeasurementProvenanceView(
                provenance.sourceType(),
                sourceName(provenance),
                method(provenance),
                reference(provenance),
                provenance.confidence()
            )
        ),
        freshnessStatus
    );
  }

  private static @Nullable String sourceName(@NonNull MeasurementProvenance provenance) {
    return provenance.sourceName() == null ? null : provenance.sourceName().value(); // NOSONAR
  }

  private static @Nullable String method(@NonNull MeasurementProvenance provenance) {
    return provenance.method() == null ? null : provenance.method().value(); // NOSONAR
  }

  private static @Nullable String reference(@NonNull MeasurementProvenance provenance) {
    return provenance.reference() == null ? null : provenance.reference().value(); // NOSONAR
  }

}
