package org.craft.mcs.cap.core.measurement.application.contract;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Read model of a retained characteristic-result retraction tombstone. */
public record MeasurementResultRetractionView(
    @NonNull MeasurementSetRevisionId sourceRevisionId,
    @NonNull MeasurementSetRevisionNumber sourceRevisionNumber,
    @NonNull MeasurementRecordView resultSnapshot,
    @NonNull List<@NonNull MeasurementRecordView> observationSnapshots,
    @NonNull ActorReference actor,
    @NonNull Instant retractedAt,
    @NonNull MeasurementCorrectionReason reason
) {

  public MeasurementResultRetractionView {
    requireNonNull(sourceRevisionId, "retracted result source revision id cannot be null");
    requireNonNull(sourceRevisionNumber, "retracted result source revision number cannot be null");
    requireNonNull(resultSnapshot, "retracted result snapshot cannot be null");
    observationSnapshots = List.copyOf(requireNonNull(
        observationSnapshots,
        "retracted observation snapshots cannot be null"
    ));
    requireNonNull(actor, "retracted result actor cannot be null");
    requireNonNull(retractedAt, "retracted result time cannot be null");
    requireNonNull(reason, "retracted result reason cannot be null");
  }

  public @NonNull MeasurementRecordId recordId() {
    return resultSnapshot.recordId();
  }

  public @NonNull MeasurementCharacteristic characteristic() {
    return resultSnapshot.characteristic();
  }
}
