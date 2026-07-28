package org.craft.mcs.cap.shared.testing;

import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
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
 * Adapts immutable measurement fixtures to historical and composite current-profile test views.
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
    return new FeedstockCurrentProfileView(
        measurement.batchId(),
        materialStateId,
        measurement.chemicalProfile(),
        evidence(measurement, materialStateId).stream()
            .map(snapshot -> new MeasurementEvidenceView(snapshot, freshnessStatus))
            .toList(),
        new FeedstockLatestMeasurementView(
            measurement.measurementId(),
            measurement.name(),
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
    final var profile = measurement.chemicalProfile();
    final var evidence = new ArrayList<MeasurementEvidenceSnapshot>();
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

  private static @NonNull MeasurementEvidenceSnapshot evidence(
      @NonNull FeedstockMeasurementFixture measurement,
      @NonNull MaterialStateId materialStateId,
      @NonNull MeasurementCharacteristic characteristic
  ) {
    return new MeasurementEvidenceSnapshot(
        characteristic,
        MeasurementValueOrigin.MEASURED,
        materialStateId,
        new MeasurementEvidenceReference(measurement.measurementId().value(), newUuid()),
        measurement.measuredAt(),
        new MeasurementProvenanceView(
            MeasurementSourceType.USER,
            null,
            null,
            null,
            null
        ),
        MeasurementEvidenceValidity.VALID
    );
  }
}
