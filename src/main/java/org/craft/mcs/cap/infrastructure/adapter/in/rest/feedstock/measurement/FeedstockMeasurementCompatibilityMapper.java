package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.core.measurement.domain.projection.MeasurementChemicalProfileProjector;
import org.craft.mcs.cap.infrastructure.adapter.in.measurement.MeasurementValueInputAcl;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.UpdateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceName;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/** Converts the deprecated Feedstock transport shape to generic immutable measurement revisions. */
@Component
@RequiredArgsConstructor
final class FeedstockMeasurementCompatibilityMapper {

  private static final String PUT_CORRECTION_REASON = "Corrected through deprecated Feedstock PUT API";
  private static final String PATCH_CORRECTION_REASON = "Corrected through deprecated Feedstock PATCH API";

  private final @NonNull MeasurementValueInputAcl valueInputAcl;

  boolean supportsLegacyChemicalProfile(@NonNull MeasurementSetView view) {
    requireNonNull(view, "measurement set view cannot be null");
    final var characteristics = view.records().stream()
        .filter(recordView -> recordView.role() == MeasurementRecordRole.RESULT)
        .map(MeasurementRecordView::characteristic)
        .toList();
    return !characteristics.isEmpty()
        && characteristics.stream()
            .allMatch(FeedstockMeasurementCompatibilityMapper::isLegacyChemicalCharacteristic);
  }

  @NonNull CreateMeasurementSetCommand toCommand(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId expectedCurrentStateId,
      @NonNull CreateFeedstockMeasurementRequest request
  ) {
    return new CreateMeasurementSetCommand(
        null,
        subject,
        expectedCurrentStateId,
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

  @NonNull FeedstockMeasurementResponse toResponse(@NonNull MeasurementSetView view) {
    final var resultRecords = resultRecords(view);
    final var profile = MeasurementChemicalProfileProjector.project(resultRecords.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().value()
            )))
        .orElseThrow();
    final var profileResponse = ChemicalProfileRestMapper.toResponse(profile);
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
        withLegacyProvenance(profileResponse, resultRecords),
        view.note()
    );
  }

  private static @NonNull FeedstockMeasurementChemicalProfileResponse withLegacyProvenance(
      @NonNull ChemicalProfileResponse profile,
      @NonNull Map<MeasurementCharacteristic, MeasurementRecordView> records
  ) {
    return new FeedstockMeasurementChemicalProfileResponse(
        profile.sugar(),
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

  private static @NonNull Map<MeasurementCharacteristic, MeasurementRecordView> resultRecords(
      @NonNull MeasurementSetView view
  ) {
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
