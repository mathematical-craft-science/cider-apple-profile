package org.craft.mcs.cap.shared.testing;

import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockProfileAxisEvidence;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/**
 * Adapts immutable fixtures to measurement views in read-side tests.
 */
public final class FeedstockMeasurementViews {

  private FeedstockMeasurementViews() {
  }

  public static @NonNull FeedstockMeasurementView view(
      @NonNull FeedstockMeasurementFixture measurement
  ) {
    final var materialStateId = MaterialStateId.newId();
    final var evidence = evidence(measurement, materialStateId);
    return new FeedstockMeasurementView(
        measurement.measurementId(),
        measurement.batchId(),
        materialStateId,
        measurement.name(),
        measurement.chemicalProfile(),
        measurement.measuredAt(),
        0,
        evidence,
        measurement.note()
    );
  }

  private static @NonNull List<@NonNull FeedstockProfileAxisEvidence> evidence(
      @NonNull FeedstockMeasurementFixture measurement,
      @NonNull MaterialStateId materialStateId
  ) {
    final var profile = measurement.chemicalProfile();
    final var evidence = new ArrayList<FeedstockProfileAxisEvidence>();
    if (profile.sugar().isPresent()) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.SPECIFIC_GRAVITY));
    }
    if (profile.ph().isPresent()) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.PH));
    }
    if (profile.titratableAcidity().isPresent()) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.TITRATABLE_ACIDITY));
    }
    if (profile.tannin().isPresent()) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.TANNIN));
    }
    if (profile.yeastAssimilableNitrogen().isPresent()) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.YAN));
    }
    return List.copyOf(evidence);
  }

  private static @NonNull FeedstockProfileAxisEvidence evidence(
      @NonNull FeedstockMeasurementFixture measurement,
      @NonNull MaterialStateId materialStateId,
      @NonNull MeasurementCharacteristic characteristic
  ) {
    return new FeedstockProfileAxisEvidence(
        characteristic,
        MeasurementValueOrigin.MEASURED,
        materialStateId,
        measurement.measurementId().value(),
        newUuid(),
        measurement.measuredAt(),
        null,
        null,
        MeasurementEvidenceValidity.VALID
    );
  }
}
