package org.craft.mcs.cap.feedstock.application.currentprofile.model;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSummaryView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class FeedstockCurrentProfileViewTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-24T00:00:00Z");

  @Test
  void returnsNoAggregateConfidenceWithoutEvidence() {
    assertThat(view(List.of()))
        .returns(null, FeedstockCurrentProfileView::aggregateConfidence);
  }

  @Test
  void returnsNoAggregateConfidenceWhenAnyAxisHasNoDeclaredConfidence() {
    assertThat(view(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.HIGH),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, null)
    )))
        .returns(null, FeedstockCurrentProfileView::aggregateConfidence);
  }

  @Test
  void returnsWeakestDeclaredAxisConfidence() {
    assertThat(view(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.LAB_VERIFIED),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, ConfidenceLevel.LOW),
        evidence(MeasurementCharacteristic.TANNIN, ConfidenceLevel.MEDIUM)
    )))
        .returns(ConfidenceLevel.LOW, FeedstockCurrentProfileView::aggregateConfidence);
  }

  @Test
  void createsCompactSummaryFromTheSameCurrentProfileEvidenceSelection() {
    final var currentProfile = view(List.of());

    assertThat(currentProfile.summary())
        .returns(currentProfile.latestMeasurement(), FeedstockCurrentProfileSummaryView::latestMeasurement)
        .returns(currentProfile.chemicalProfile(), FeedstockCurrentProfileSummaryView::chemicalProfile);
  }

  private static @NonNull FeedstockCurrentProfileView view(
      @NonNull List<@NonNull MeasurementEvidenceView> evidence
  ) {
    return new FeedstockCurrentProfileView(
        FeedstockBatchId.newId(),
        MaterialStateId.newId(),
        ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build(),
        evidence,
        new FeedstockLatestMeasurementView(
            FeedstockMeasurementId.newId(),
            FeedstockMeasurementName.of("Current profile"),
            MEASURED_AT,
            0,
            null
        )
    );
  }

  private static @NonNull MeasurementEvidenceView evidence(
      @NonNull MeasurementCharacteristic characteristic,
      @Nullable ConfidenceLevel confidence
  ) {
    return new MeasurementEvidenceView(
        new MeasurementEvidenceSnapshot(
            characteristic,
            MeasurementValueOrigin.MEASURED,
            MaterialStateId.newId(),
            new MeasurementEvidenceReference(newUuid(), newUuid()),
            MEASURED_AT,
            new MeasurementProvenanceView(
                MeasurementSourceType.USER,
                null,
                null,
                null,
                confidence
            ),
            MeasurementEvidenceValidity.VALID
        ),
        MeasurementFreshnessStatus.CURRENT
    );
  }
}
