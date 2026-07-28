package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import java.util.EnumMap;
import java.util.Map;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Selects the generic result records representable by the deprecated Feedstock chemical profile. */
final class FeedstockMeasurementCompatibilityRecords {

  private FeedstockMeasurementCompatibilityRecords() {
  }

  static boolean supportsLegacyChemicalProfile(@NonNull MeasurementSetView view) {
    requireNonNull(view, "measurement set view cannot be null");
    final var characteristics = view.records().stream()
        .filter(recordView -> recordView.role() == MeasurementRecordRole.RESULT)
        .map(MeasurementRecordView::characteristic)
        .toList();
    return !characteristics.isEmpty()
        && characteristics.stream()
            .allMatch(FeedstockMeasurementCompatibilityRecords::isLegacyChemicalCharacteristic);
  }

  static @NonNull Map<MeasurementCharacteristic, MeasurementRecordView> resultRecords(
      @NonNull MeasurementSetView view
  ) {
    requireNonNull(view, "measurement set view cannot be null");
    final var records = new EnumMap<MeasurementCharacteristic, MeasurementRecordView>(
        MeasurementCharacteristic.class
    );
    view.records().stream()
        .filter(recordView -> recordView.role() == MeasurementRecordRole.RESULT)
        .filter(recordView -> isLegacyChemicalCharacteristic(recordView.characteristic()))
        .forEach(recordView -> records.put(recordView.characteristic(), recordView));
    return Map.copyOf(records);
  }

  private static boolean isLegacyChemicalCharacteristic(
      @NonNull MeasurementCharacteristic characteristic
  ) {
    return switch (characteristic) {
      case SPECIFIC_GRAVITY, PH, TITRATABLE_ACIDITY, TANNIN, YAN -> true;
      case TEMPERATURE, TURBIDITY -> false;
    };
  }
}
