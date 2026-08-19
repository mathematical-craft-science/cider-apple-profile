package org.craft.mcs.cap.feedstock.batch.application.currentprofile.service;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityIndicator;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityIndicatorCode;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityStatus;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.service.FeedstockProfileDataQualityAssessor;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FeedstockProfileDataQualityAssessorTest {

  private final FeedstockProfileDataQualityAssessor assessor = new FeedstockProfileDataQualityAssessor();

  @Test
  void doesNotReportMissingConfidenceForCharacteristicsWithoutMeasurements() {
    final var currentProfile = mock(FeedstockCurrentProfileView.class);
    final var graphEvaluation = mock(JolicoeurSugarAcidityGraphEvaluation.class);
    final var evidence = List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.HIGH),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, ConfidenceLevel.MEDIUM)
    );
    when(graphEvaluation.plottable()).thenReturn(true);

    final var dataQuality = assessor.assess(
        currentProfile,
        graphEvaluation,
        new FeedstockCurrentProfileFreshnessView(MeasurementFreshnessStatus.CURRENT, null),
        evidence
    );

    assertThat(dataQuality)
        .returns(
            FeedstockCurrentProfileDataQualityStatus.NEEDS_ATTENTION,
            FeedstockCurrentProfileDataQualityView::status
        )
        .extracting(FeedstockCurrentProfileDataQualityView::indicators)
        .satisfies(indicators -> assertThat(indicators)
            .extracting(FeedstockCurrentProfileDataQualityIndicator::code)
            .contains(
                FeedstockCurrentProfileDataQualityIndicatorCode.TANNIN_MISSING,
                FeedstockCurrentProfileDataQualityIndicatorCode.PH_MISSING,
                FeedstockCurrentProfileDataQualityIndicatorCode.YAN_MISSING
            )
            .doesNotContain(FeedstockCurrentProfileDataQualityIndicatorCode.CONFIDENCE_MISSING));
  }

  private static @NonNull MeasurementEvidenceView evidence(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull ConfidenceLevel confidence
  ) {
    return new MeasurementEvidenceView(
        characteristic,
        MeasurementValueOrigin.MEASURED,
        MaterialStateId.newId(),
        new MeasurementEvidenceReference(newUuid(), newUuid()),
        Instant.parse("2026-08-05T10:00:00Z"),
        new MeasurementProvenanceView(
            MeasurementSourceType.USER,
            null,
            null,
            null,
            confidence
        ),
        MeasurementFreshnessStatus.CURRENT
    );
  }
}
