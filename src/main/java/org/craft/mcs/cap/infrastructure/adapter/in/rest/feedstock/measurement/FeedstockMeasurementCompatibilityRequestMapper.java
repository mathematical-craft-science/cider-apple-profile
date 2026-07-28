package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.PatchMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.ReplaceMeasurementSetCommand;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.infrastructure.adapter.in.measurement.MeasurementValueInputAcl;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.UpdateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceName;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement
    .FeedstockMeasurementCompatibilityRecords.resultRecords;


/** Converts deprecated Feedstock requests to generic immutable measurement commands. */
@Component
@RequiredArgsConstructor
final class FeedstockMeasurementCompatibilityRequestMapper {

  private static final String PUT_CORRECTION_REASON = "Corrected through deprecated Feedstock PUT API";
  private static final String PATCH_CORRECTION_REASON = "Corrected through deprecated Feedstock PATCH API";

  private final @NonNull MeasurementValueInputAcl valueInputAcl;

  @NonNull CreateMeasurementSetCommand toCommand(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId observedMaterialStateId,
      @NonNull CreateFeedstockMeasurementRequest request
  ) {
    return new CreateMeasurementSetCommand(
        null,
        subject,
        observedMaterialStateId,
        MeasurementSetName.of(request.name()),
        request.note(),
        inputs(request.chemicalProfile(), request.measuredAt(), Map.of())
    );
  }

  @NonNull ReplaceMeasurementSetCommand toCommand(
      @NonNull MeasurementSetView current,
      @NonNull UpdateFeedstockMeasurementRequest request
  ) {
    return new ReplaceMeasurementSetCommand(
        current.measurementSetId(),
        current.version(),
        MeasurementSetName.of(request.name()),
        request.note(),
        MeasurementCorrectionReason.of(PUT_CORRECTION_REASON),
        inputs(request.chemicalProfile(), request.measuredAt(), resultRecords(current))
    );
  }

  @NonNull PatchMeasurementSetCommand toCommand(
      @NonNull MeasurementSetView current,
      @NonNull PatchFeedstockMeasurementRequest request
  ) {
    final var currentResults = resultRecords(current);
    final var replacements = new EnumMap<MeasurementCharacteristic, MeasurementCharacteristicInput>(
        MeasurementCharacteristic.class
    );
    if (request.measuredAt() instanceof Instant measuredAt) {
      currentResults.forEach((characteristic, recordView) -> replacements.put(
          characteristic,
          directInput(
              characteristic,
              recordView.value(),
              measuredAt,
              recordView.provenance()
          )
      ));
    }
    if (request.chemicalProfile() instanceof FeedstockMeasurementChemicalProfileRequest profile) {
      inputs(
          profile,
          replacementTime(request, currentResults),
          currentResults
      ).forEach(input -> replacements.put(input.characteristic(), input));
    }
    return new PatchMeasurementSetCommand(
        current.measurementSetId(),
        current.version(),
        request.name() == null ? null : MeasurementSetName.of(request.name()), // NOSONAR
        request.note() != null,
        normalizeNote(request.note()),
        MeasurementCorrectionReason.of(PATCH_CORRECTION_REASON),
        List.copyOf(replacements.values()),
        Set.of()
    );
  }

  private @NonNull List<@NonNull MeasurementCharacteristicInput> inputs(
      @NonNull FeedstockMeasurementChemicalProfileRequest profile,
      @NonNull Instant measuredAt,
      @NonNull Map<MeasurementCharacteristic, MeasurementRecordView> current
  ) {
    final var inputs = new ArrayList<MeasurementCharacteristicInput>();
    addInput(inputs, MeasurementCharacteristic.SPECIFIC_GRAVITY, profile.sugar(), profile, measuredAt, current);
    addInput(inputs, MeasurementCharacteristic.PH, profile.ph(), profile, measuredAt, current);
    addInput(
        inputs,
        MeasurementCharacteristic.TITRATABLE_ACIDITY,
        profile.titratableAcidity(),
        profile,
        measuredAt,
        current
    );
    addInput(inputs, MeasurementCharacteristic.TANNIN, profile.tannin(), profile, measuredAt, current);
    addInput(
        inputs,
        MeasurementCharacteristic.YAN,
        profile.yeastAssimilableNitrogen(),
        profile,
        measuredAt,
        current
    );
    return List.copyOf(inputs);
  }

  private void addInput(
      @NonNull List<MeasurementCharacteristicInput> inputs,
      @NonNull MeasurementCharacteristic characteristic,
      @Nullable ChemicalMeasurementRequest request,
      @NonNull FeedstockMeasurementChemicalProfileRequest profile,
      @NonNull Instant measuredAt,
      @NonNull Map<MeasurementCharacteristic, MeasurementRecordView> current
  ) {
    if (request == null) {
      return;
    }
    final var currentRecord = current.get(characteristic);
    inputs.add(directInput(
        characteristic,
        toCanonicalValue(
            characteristic,
            request,
            CanonicalUnitLabels.forCharacteristic(characteristic)
        ),
        measuredAt,
        provenance(profile, currentRecord)
    ));
  }

  private static @NonNull MeasurementCharacteristicInput directInput(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull Instant measuredAt,
      @NonNull MeasurementProvenance provenance
  ) {
    return MeasurementCharacteristicInput.direct(characteristic, value, measuredAt, provenance);
  }

  private @NonNull CharacteristicValue<MeasuredValue> toCanonicalValue(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull ChemicalMeasurementRequest request,
      @NonNull String defaultUnit
  ) {
    return switch (request) {
      case ExactChemicalMeasurementRequest(_, var unit, var measuredValue) -> valueInputAcl.point(
          characteristic,
          measuredValue,
          unit == null ? defaultUnit : unit
      );
      case RangeChemicalMeasurementRequest(_, var unit, var minimum, var maximum) -> valueInputAcl.interval(
          characteristic,
          minimum,
          maximum,
          unit == null ? defaultUnit : unit
      );
    };
  }

  private static @NonNull MeasurementProvenance provenance(
      @NonNull FeedstockMeasurementChemicalProfileRequest profile,
      @Nullable MeasurementRecordView current
  ) {
    if (profile.source() == null && profile.confidence() == null && current != null) {
      return current.provenance();
    }
    return new MeasurementProvenance(
        MeasurementSourceType.USER,
        sourceName(profile.source()),
        null,
        null,
        profile.confidence()
    );
  }

  private static @NonNull Instant replacementTime(
      @NonNull PatchFeedstockMeasurementRequest request,
      @NonNull Map<MeasurementCharacteristic, MeasurementRecordView> current
  ) {
    if (request.measuredAt() instanceof Instant measuredAt) {
      return measuredAt;
    }
    return current.values().stream()
        .map(MeasurementRecordView::measuredAt)
        .max(Instant::compareTo)
        .orElseThrow();
  }

  private static @Nullable String normalizeNote(@Nullable String note) {
    if (note == null) {
      return null;
    }
    final var normalized = note.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
