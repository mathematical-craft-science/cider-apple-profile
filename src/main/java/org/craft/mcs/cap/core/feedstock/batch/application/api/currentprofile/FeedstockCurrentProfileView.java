package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * State-aware composite current profile of one Feedstock batch.
 *
 * <p>Characteristics may originate from different measurement sets along the current material
 * lineage. {@link #latestMeasurementSet()} is only the latest contributing measurement metadata;
 * {@link #evidence()} remains the authority for each characteristic.
 */
public record FeedstockCurrentProfileView(
    @NonNull FeedstockBatchId batchId,
    @NonNull MaterialStateId currentMaterialStateId,
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<PhValue> ph,
    @Nullable CharacteristicValue<TanninValue> tannin,
    @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen,
    @NonNull List<@NonNull MeasurementEvidenceView> evidence,
    @NonNull FeedstockLatestMeasurementSetView latestMeasurementSet
) {

  private static final Set<MeasurementCharacteristic> PROFILE_CHARACTERISTICS = Set.copyOf(
      EnumSet.of(
          MeasurementCharacteristic.SPECIFIC_GRAVITY,
          MeasurementCharacteristic.TITRATABLE_ACIDITY,
          MeasurementCharacteristic.PH,
          MeasurementCharacteristic.TANNIN,
          MeasurementCharacteristic.YAN
      )
  );

  public FeedstockCurrentProfileView {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(currentMaterialStateId, "feedstock current material state id cannot be null");
    evidence = List.copyOf(requireNonNull(evidence, "feedstock current profile evidence cannot be null"));
    requireNonNull(latestMeasurementSet, "latest feedstock measurement set cannot be null");
    if (evidence.stream().anyMatch(item -> !PROFILE_CHARACTERISTICS.contains(item.characteristic()))) {
      throw new ApplicationValidationException(
          "feedstock current profile accepts only SG, TA, pH, tannin and YAN evidence"
      );
    }
    validateCharacteristicEvidence(
        specificGravity,
        titratableAcidity,
        ph,
        tannin,
        yeastAssimilableNitrogen,
        evidence
    );
    validateLatestMeasurement(evidence, latestMeasurementSet);
  }

  /**
   * Returns the weakest declared confidence across selected characteristics.
   *
   * @return weakest confidence, or {@code null} when evidence is absent or any item has no
   *     declared confidence
   */
  public @Nullable ConfidenceLevel aggregateConfidence() {
    var weakest = ConfidenceLevel.LAB_VERIFIED;
    for (final var item : evidence) {
      final var confidence = item.provenance().confidence();
      if (confidence == null) {
        return null;
      }
      if (confidence.compareTo(weakest) < 0) {
        weakest = confidence;
      }
    }
    return weakest;
  }

  /**
   * Selects provenance for the characteristics used by a concrete analysis scenario.
   */
  public @NonNull List<@NonNull MeasurementEvidenceView> evidenceFor(
      @NonNull Set<@NonNull MeasurementCharacteristic> characteristics
  ) {
    requireNonNull(characteristics, "feedstock evidence characteristics cannot be null");
    return evidence.stream()
        .filter(item -> characteristics.contains(item.characteristic()))
        .toList();
  }

  /**
   * Returns the compact current-profile projection shared by feedstock read scenarios.
   */
  public @NonNull FeedstockCurrentProfileSummaryView summary() {
    return new FeedstockCurrentProfileSummaryView(
        latestMeasurementSet,
        specificGravity,
        titratableAcidity,
        ph,
        tannin,
        yeastAssimilableNitrogen
    );
  }

  private static void validateCharacteristicEvidence(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
      @Nullable CharacteristicValue<PhValue> ph,
      @Nullable CharacteristicValue<TanninValue> tannin,
      @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen,
      @NonNull List<@NonNull MeasurementEvidenceView> evidence
  ) {
    final var expectedCharacteristics = EnumSet.noneOf(MeasurementCharacteristic.class);
    addWhenPresent(expectedCharacteristics, MeasurementCharacteristic.SPECIFIC_GRAVITY, specificGravity);
    addWhenPresent(expectedCharacteristics, MeasurementCharacteristic.TITRATABLE_ACIDITY, titratableAcidity);
    addWhenPresent(expectedCharacteristics, MeasurementCharacteristic.PH, ph);
    addWhenPresent(expectedCharacteristics, MeasurementCharacteristic.TANNIN, tannin);
    addWhenPresent(expectedCharacteristics, MeasurementCharacteristic.YAN, yeastAssimilableNitrogen);

    final var evidenceByCharacteristic = evidence.stream()
        .collect(Collectors.groupingBy(MeasurementEvidenceView::characteristic));
    final var actualCharacteristics = evidenceByCharacteristic.keySet();
    if (expectedCharacteristics.isEmpty() || !expectedCharacteristics.equals(actualCharacteristics)) {
      throw new ApplicationValidationException(
          "feedstock current profile values and evidence characteristics must match"
      );
    }
    if (evidenceByCharacteristic.values().stream().anyMatch(items -> items.size() != 1)) {
      throw new ApplicationValidationException(
          "feedstock current profile requires exactly one evidence item per characteristic"
      );
    }
  }

  private static void validateLatestMeasurement(
      @NonNull List<@NonNull MeasurementEvidenceView> evidence,
      @NonNull FeedstockLatestMeasurementSetView latestMeasurement
  ) {
    final var latestMeasuredAt = evidence.stream()
        .map(MeasurementEvidenceView::measuredAt)
        .max(Comparable::compareTo)
        .orElseThrow();
    final var representsLatestContributor = evidence.stream()
        .anyMatch(item -> item.measuredAt().equals(latestMeasuredAt)
            && item.evidenceReference().measurementSetId().equals(latestMeasurement.measurementSetId()));
    if (!latestMeasurement.measuredAt().equals(latestMeasuredAt) || !representsLatestContributor) {
      throw new ApplicationValidationException(
          "latest feedstock measurement must identify a latest contributing evidence item"
      );
    }
  }

  private static void addWhenPresent(
      @NonNull EnumSet<MeasurementCharacteristic> characteristics,
      @NonNull MeasurementCharacteristic characteristic,
      @Nullable Object value
  ) {
    if (value != null) {
      characteristics.add(characteristic);
    }
  }
}
