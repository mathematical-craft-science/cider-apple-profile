package org.craft.mcs.cap.shared.testing;

import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/**
 * Adapts immutable measurement fixtures to composite current-profile test views.
 */
public final class FeedstockCurrentProfileViews {

  private FeedstockCurrentProfileViews() {
  }

  public static @NonNull FeedstockCurrentProfileView currentProfile(
      @NonNull FeedstockMeasurementFixture measurement
  ) {
    return currentProfile(measurement, MeasurementFreshnessStatus.CURRENT);
  }

  public static @NonNull FeedstockCurrentProfileView currentProfile(
      @NonNull FeedstockMeasurementFixture measurement,
      @NonNull MeasurementFreshnessStatus freshnessStatus
  ) {
    return currentProfile(measurement, MaterialStateId.newId(), freshnessStatus);
  }

  public static @NonNull FeedstockCurrentProfileView currentProfile(
      @NonNull FeedstockMeasurementFixture measurement,
      @NonNull MaterialStateId materialStateId
  ) {
    return currentProfile(measurement, materialStateId, MeasurementFreshnessStatus.CURRENT);
  }

  public static @NonNull FeedstockCurrentProfileView currentProfile(
      @NonNull FeedstockMeasurementFixture measurement,
      @NonNull MaterialStateId materialStateId,
      @NonNull MeasurementFreshnessStatus freshnessStatus
  ) {
    final var profile = measurement.characteristics();
    return new FeedstockCurrentProfileView(
        measurement.batchId(),
        materialStateId,
        profile.specificGravity(),
        profile.titratableAcidity(),
        profile.ph(),
        profile.tannin(),
        profile.yeastAssimilableNitrogen(),
        evidence(measurement, materialStateId).stream()
            .map(snapshot -> new MeasurementEvidenceView(snapshot, freshnessStatus))
            .toList(),
        new FeedstockLatestMeasurementSetView(
            measurement.measurementId().value(),
            measurement.name().value(),
            measurement.measuredAt(),
            0,
            measurement.note()
        )
    );
  }

  private static @NonNull List<@NonNull MeasurementEvidenceSnapshot> evidence(
      @NonNull FeedstockMeasurementFixture measurement,
      @NonNull MaterialStateId materialStateId
  ) {
    final var profile = measurement.characteristics();
    final var evidence = new ArrayList<MeasurementEvidenceSnapshot>();
    if (profile.specificGravity() != null) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.SPECIFIC_GRAVITY));
    }
    if (profile.ph() != null) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.PH));
    }
    if (profile.titratableAcidity() != null) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.TITRATABLE_ACIDITY));
    }
    if (profile.tannin() != null) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.TANNIN));
    }
    if (profile.yeastAssimilableNitrogen() != null) {
      evidence.add(evidence(measurement, materialStateId, MeasurementCharacteristic.YAN));
    }
    return List.copyOf(evidence);
  }

  private static @NonNull MeasurementEvidenceSnapshot evidence(
      @NonNull FeedstockMeasurementFixture measurement,
      @NonNull MaterialStateId materialStateId,
      @NonNull MeasurementCharacteristic characteristic
  ) {
    return new MeasurementEvidenceSnapshot(
        characteristic,
        MeasurementValueOrigin.MEASURED,
        materialStateId,
        new MeasurementEvidenceReference(
            measurement.measurementId().value(),
            newUuid()
        ),
        measurement.measuredAt(),
        new MeasurementProvenanceView(
            MeasurementSourceType.USER,
            null,
            null,
            null,
            null
        )
    );
  }
}
