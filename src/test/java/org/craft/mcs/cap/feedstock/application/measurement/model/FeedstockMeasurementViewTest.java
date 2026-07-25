package org.craft.mcs.cap.feedstock.application.measurement.model;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockProfileAxisEvidence;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class FeedstockMeasurementViewTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-24T00:00:00Z");

  @Test
  void returnsNoAggregateConfidenceWithoutEvidence() {
    assertThat(view(List.of()))
        .returns(null, FeedstockMeasurementView::aggregateConfidence);
  }

  @Test
  void returnsNoAggregateConfidenceWhenAnyAxisHasNoDeclaredConfidence() {
    assertThat(view(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.HIGH),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, null)
    )))
        .returns(null, FeedstockMeasurementView::aggregateConfidence);
  }

  @Test
  void returnsWeakestDeclaredAxisConfidence() {
    assertThat(view(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.LAB_VERIFIED),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, ConfidenceLevel.LOW),
        evidence(MeasurementCharacteristic.TANNIN, ConfidenceLevel.MEDIUM)
    )))
        .returns(ConfidenceLevel.LOW, FeedstockMeasurementView::aggregateConfidence);
  }

  private static @NonNull FeedstockMeasurementView view(
      @NonNull List<@NonNull FeedstockProfileAxisEvidence> evidence
  ) {
    return new FeedstockMeasurementView(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        MaterialStateId.newId(),
        FeedstockMeasurementName.of("Current profile"),
        ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build(),
        MEASURED_AT,
        0,
        evidence,
        null
    );
  }

  private static @NonNull FeedstockProfileAxisEvidence evidence(
      @NonNull MeasurementCharacteristic characteristic,
      @Nullable ConfidenceLevel confidence
  ) {
    return new FeedstockProfileAxisEvidence(
        characteristic,
        MeasurementValueOrigin.MEASURED,
        MaterialStateId.newId(),
        newUuid(),
        newUuid(),
        MEASURED_AT,
        null,
        confidence,
        MeasurementEvidenceValidity.VALID
    );
  }
}
