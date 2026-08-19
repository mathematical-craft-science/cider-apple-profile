package org.craft.mcs.cap.feedstock.batch.application.currentprofile.service;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.service.FeedstockCurrentProfileFreshnessAssessor;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FeedstockCurrentProfileFreshnessAssessorTest {

  private final FeedstockCurrentProfileFreshnessAssessor assessor =
      new FeedstockCurrentProfileFreshnessAssessor();

  @Test
  void reportsStaleWhenAnyPlanningCharacteristicIsStale() {
    final var profile = profile(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, MeasurementFreshnessStatus.STALE),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, MeasurementFreshnessStatus.CURRENT)
    ));

    assertThat(assessor.assess(profile))
        .returns(MeasurementFreshnessStatus.STALE, FeedstockCurrentProfileFreshnessView::status)
        .doesNotReturn(null, FeedstockCurrentProfileFreshnessView::reason);
  }

  @Test
  void ignoresStaleEvidenceThatDoesNotParticipateInFeedstockPlanningCoordinates() {
    final var profile = profile(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, MeasurementFreshnessStatus.CURRENT),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, MeasurementFreshnessStatus.CURRENT),
        evidence(MeasurementCharacteristic.YAN, MeasurementFreshnessStatus.STALE)
    ));

    assertThat(assessor.assess(profile))
        .returns(MeasurementFreshnessStatus.CURRENT, FeedstockCurrentProfileFreshnessView::status)
        .returns(null, FeedstockCurrentProfileFreshnessView::reason);
  }

  private static @NonNull FeedstockCurrentProfileView profile(
      @NonNull List<@NonNull MeasurementEvidenceView> evidence
  ) {
    final var profile = mock(FeedstockCurrentProfileView.class);
    when(profile.evidence()).thenReturn(evidence);
    return profile;
  }

  private static @NonNull MeasurementEvidenceView evidence(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull MeasurementFreshnessStatus freshnessStatus
  ) {
    return new MeasurementEvidenceView(
        characteristic,
        MeasurementValueOrigin.MEASURED,
        MaterialStateId.newId(),
        new MeasurementEvidenceReference(newUuid(), newUuid()),
        Instant.parse("2026-07-01T10:00:00Z"),
        new MeasurementProvenanceView(
            MeasurementSourceType.USER,
            null,
            null,
            null,
            null
        ),
        freshnessStatus
    );
  }
}
