package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.measurement.application.compatibility.MeasurementChemicalProfileProjector;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileResponseRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement
    .FeedstockMeasurementCompatibilityRecords.resultRecords;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/** Reconstructs deprecated Feedstock responses from generic immutable measurement evidence. */
final class FeedstockMeasurementCompatibilityResponseMapper {

  private FeedstockMeasurementCompatibilityResponseMapper() {
  }

  static @NonNull FeedstockMeasurementResponse toResponse(@NonNull MeasurementSetView view) {
    final var resultRecords = resultRecords(view);
    final var profile = MeasurementChemicalProfileProjector.project(resultRecords.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().value()
            )))
        .orElseThrow();
    final var measuredAt = resultRecords.values().stream()
        .map(MeasurementRecordView::measuredAt)
        .max(Instant::compareTo)
        .orElse(view.revisionCreatedAt());
    return new FeedstockMeasurementResponse(
        view.measurementSetId().value(),
        view.workspaceId().value(),
        view.subject().id(),
        view.name().value(),
        measuredAt,
        withLegacyProvenance(ChemicalProfileResponseRestMapper.toResponse(profile), resultRecords),
        view.note()
    );
  }

  private static @NonNull FeedstockMeasurementChemicalProfileResponse withLegacyProvenance(
      @NonNull ChemicalProfileResponse profile,
      @NonNull Map<MeasurementCharacteristic, MeasurementRecordView> records
  ) {
    return new FeedstockMeasurementChemicalProfileResponse(
        profile.specificGravity(),
        profile.ph(),
        profile.titratableAcidity(),
        profile.tannin(),
        profile.yeastAssimilableNitrogen(),
        sourceNameValue(commonValue(records, recordView -> recordView.provenance().sourceName())),
        commonValue(records, recordView -> recordView.provenance().confidence())
    );
  }

  private static <T> @Nullable T commonValue(
      @NonNull Map<MeasurementCharacteristic, MeasurementRecordView> records,
      @NonNull Function<MeasurementRecordView, T> extractor
  ) {
    final var values = records.values().stream()
        .map(extractor)
        .distinct()
        .toList();
    return values.size() == 1 ? values.getFirst() : null;
  }
}
