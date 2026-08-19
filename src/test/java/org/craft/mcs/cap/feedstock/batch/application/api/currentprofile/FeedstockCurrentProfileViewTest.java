package org.craft.mcs.cap.feedstock.batch.application.api.currentprofile;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSummaryView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class FeedstockCurrentProfileViewTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-24T00:00:00Z");

  @Test
  void rejectsProfileWithoutMatchingEvidence() {
    assertThatThrownBy(() -> new FeedstockCurrentProfileView(
        FeedstockBatchId.newId(),
        MaterialStateId.newId(),
        PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
        null,
        null,
        null,
        null,
        List.of(),
        latestMeasurement(MeasurementSetId.newId())
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("values and evidence characteristics must match");
  }

  @Test
  void returnsNoAggregateConfidenceWhenAnyCharacteristicHasNoDeclaredConfidence() {
    assertThat(view(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.HIGH),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, null)
    )))
        .returns(null, FeedstockCurrentProfileView::aggregateConfidence);
  }

  @Test
  void returnsWeakestDeclaredCharacteristicConfidence() {
    assertThat(view(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.LAB_VERIFIED),
        evidence(MeasurementCharacteristic.TITRATABLE_ACIDITY, ConfidenceLevel.LOW),
        evidence(MeasurementCharacteristic.TANNIN, ConfidenceLevel.MEDIUM)
    )))
        .returns(ConfidenceLevel.LOW, FeedstockCurrentProfileView::aggregateConfidence);
  }

  @Test
  void createsCompactSummaryFromTheSameCurrentProfileEvidenceSelection() {
    final var currentProfile = view(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.HIGH)
    ));

    assertThat(currentProfile.summary())
        .returns(
            currentProfile.latestMeasurementSet(),
            FeedstockCurrentProfileSummaryView::latestMeasurementSet
        )
        .returns(currentProfile.specificGravity(), FeedstockCurrentProfileSummaryView::specificGravity)
        .returns(currentProfile.titratableAcidity(), FeedstockCurrentProfileSummaryView::titratableAcidity)
        .returns(currentProfile.ph(), FeedstockCurrentProfileSummaryView::ph)
        .returns(currentProfile.tannin(), FeedstockCurrentProfileSummaryView::tannin)
        .returns(
            currentProfile.yeastAssimilableNitrogen(),
            FeedstockCurrentProfileSummaryView::yeastAssimilableNitrogen
        );
  }

  @Test
  void rejectsNonProfileEvidence() {
    final var temperatureEvidence = evidence(MeasurementCharacteristic.TEMPERATURE, ConfidenceLevel.HIGH);

    assertThatThrownBy(() -> view(List.of(temperatureEvidence)))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("SG, TA, pH, tannin and YAN");
  }

  @Test
  void rejectsDuplicateEvidenceForOneCharacteristic() {
    assertThatThrownBy(() -> view(List.of(
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.HIGH),
        evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.HIGH)
    )))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("exactly one evidence item");
  }

  @Test
  void rejectsLatestMeasurementThatIsNotAContributor() {
    final var evidence = evidence(MeasurementCharacteristic.SPECIFIC_GRAVITY, ConfidenceLevel.HIGH);

    assertThatThrownBy(() -> new FeedstockCurrentProfileView(
        FeedstockBatchId.newId(),
        MaterialStateId.newId(),
        PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
        null,
        null,
        null,
        null,
        List.of(evidence),
        latestMeasurement(MeasurementSetId.newId())
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("latest contributing evidence");
  }

  private static @NonNull FeedstockCurrentProfileView view(
      @NonNull List<@NonNull MeasurementEvidenceView> evidence
  ) {
    final var characteristics = evidence.stream()
        .map(MeasurementEvidenceView::characteristic)
        .collect(Collectors.toUnmodifiableSet());
    final var measurementId = evidence.isEmpty()
        ? MeasurementSetId.newId()
        : MeasurementSetId.of(evidence.getFirst().evidenceReference().measurementSetId());
    return new FeedstockCurrentProfileView(
        FeedstockBatchId.newId(),
        MaterialStateId.newId(),
        characteristics.contains(MeasurementCharacteristic.SPECIFIC_GRAVITY)
            ? PointCharacteristicValue.of(SpecificGravityValue.of("1.050"))
            : null,
        characteristics.contains(MeasurementCharacteristic.TITRATABLE_ACIDITY)
            ? PointCharacteristicValue.of(TitratableAcidityValue.of("6.0"))
            : null,
        characteristics.contains(MeasurementCharacteristic.PH)
            ? PointCharacteristicValue.of(PhValue.of("3.40"))
            : null,
        characteristics.contains(MeasurementCharacteristic.TANNIN)
            ? PointCharacteristicValue.of(TanninValue.of("1200"))
            : null,
        characteristics.contains(MeasurementCharacteristic.YAN)
            ? PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("180"))
            : null,
        evidence,
        latestMeasurement(measurementId)
    );
  }

  private static @NonNull FeedstockLatestMeasurementSetView latestMeasurement(
      @NonNull MeasurementSetId measurementId
  ) {
    return new FeedstockLatestMeasurementSetView(
        measurementId.value(),
        "Current profile",
        MEASURED_AT,
        0,
        null
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
            )
        ),
        MeasurementFreshnessStatus.CURRENT
    );
  }
}
