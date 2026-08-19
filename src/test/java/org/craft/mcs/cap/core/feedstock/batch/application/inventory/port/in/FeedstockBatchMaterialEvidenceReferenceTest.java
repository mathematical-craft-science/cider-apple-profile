package org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockBatchMaterialEvidenceReferenceTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-25T10:00:00Z");

  @Test
  void representsTheCompleteEvidenceSetIndependentlyOfInputOrder() {
    final var sugar = evidence(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        "019f9e1b-d2de-7000-8000-000000000001"
    );
    final var acidity = evidence(
        MeasurementCharacteristic.TITRATABLE_ACIDITY,
        "019f9e1b-d2de-7000-8000-000000000002"
    );

    assertThat(FeedstockBatchMaterialEvidenceReference.from(List.of(sugar, acidity)))
        .isEqualTo(FeedstockBatchMaterialEvidenceReference.from(List.of(acidity, sugar)))
        .isNotEqualTo(FeedstockBatchMaterialEvidenceReference.from(List.of(sugar)));
  }

  @Test
  void rejectsMoreThanOneEvidenceRecordForOneCharacteristic() {
    final var first = evidence(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        "019f9e1b-d2de-7000-8000-000000000001"
    );
    final var second = evidence(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        "019f9e1b-d2de-7000-8000-000000000002"
    );

    assertThatThrownBy(() -> FeedstockBatchMaterialEvidenceReference.from(List.of(first, second)))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("one evidence record per characteristic");
  }

  private static MeasurementEvidenceSnapshot evidence(
      MeasurementCharacteristic characteristic,
      String recordId
  ) {
    return new MeasurementEvidenceSnapshot(
        characteristic,
        MeasurementValueOrigin.MEASURED,
        MaterialStateId.newId(),
        new MeasurementEvidenceReference(
            UUID.fromString("019f9e1b-d2de-7000-8000-000000000010"),
            UUID.fromString(recordId)
        ),
        MEASURED_AT,
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
