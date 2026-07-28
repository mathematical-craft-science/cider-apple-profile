package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import java.util.List;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FeedstockMeasurementCompatibilityRecordsTest {

  @Test
  void acceptsOnlyNonEmptyLegacyChemicalResultSets() {
    final var specificGravity = result(MeasurementCharacteristic.SPECIFIC_GRAVITY);
    final var view = viewWith(specificGravity);

    assertThat(FeedstockMeasurementCompatibilityRecords.supportsLegacyChemicalProfile(view)).isTrue();
    assertThat(FeedstockMeasurementCompatibilityRecords.resultRecords(view))
        .containsOnlyKeys(MeasurementCharacteristic.SPECIFIC_GRAVITY);
  }

  @Test
  void rejectsMixedLegacyAndGenericResultSets() {
    final var view = viewWith(
        result(MeasurementCharacteristic.SPECIFIC_GRAVITY),
        result(MeasurementCharacteristic.TEMPERATURE)
    );

    assertThat(FeedstockMeasurementCompatibilityRecords.supportsLegacyChemicalProfile(view)).isFalse();
  }

  private static MeasurementSetView viewWith(MeasurementRecordView... records) {
    final var view = mock(MeasurementSetView.class);
    when(view.records()).thenReturn(List.of(records));
    return view;
  }

  private static MeasurementRecordView result(MeasurementCharacteristic characteristic) {
    final var result = mock(MeasurementRecordView.class);
    when(result.role()).thenReturn(MeasurementRecordRole.RESULT);
    when(result.characteristic()).thenReturn(characteristic);
    return result;
  }
}
