package org.craft.mcs.cap.core.measurement.domain.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Enforces evidence and metadata continuity between immutable measurement-set revisions. */
final class MeasurementSetRevisionTransitionPolicy {

  private MeasurementSetRevisionTransitionPolicy() {
  }

  static void requireValid(
      @NonNull MeasurementSetRevision current,
      @NonNull MeasurementSetRevision correction
  ) {
    final var currentActive = activeResults(current);
    final var currentRetracted = retractionsByCharacteristic(current);
    final var correctedActiveResults = activeResults(correction);
    final var correctedActive = correctedActiveResults.keySet();
    final var correctedRetracted = retractionsByCharacteristic(correction);
    final var currentRecords = recordsById(current);

    validateEvidenceIdentityContinuity(current, correction);
    validateRetractedResultReplacement(currentRetracted, correctedActiveResults);

    final var silentlyRemoved = EnumSet.noneOf(MeasurementCharacteristic.class);
    silentlyRemoved.addAll(currentActive.keySet());
    silentlyRemoved.removeAll(correctedActive);
    silentlyRemoved.removeAll(correctedRetracted.keySet());
    if (!silentlyRemoved.isEmpty()) {
      throw new DomainConflictException(
          "removed measurement results require retained retraction tombstones"
      );
    }

    final var discardedRetractions = EnumSet.noneOf(MeasurementCharacteristic.class);
    discardedRetractions.addAll(currentRetracted.keySet());
    discardedRetractions.removeAll(correctedActive);
    discardedRetractions.removeAll(correctedRetracted.keySet());
    if (!discardedRetractions.isEmpty()) {
      throw new DomainConflictException(
          "measurement result retractions must remain until replacement evidence is recorded"
      );
    }

    final var unsupportedRetractions = EnumSet.noneOf(MeasurementCharacteristic.class);
    unsupportedRetractions.addAll(correctedRetracted.keySet());
    unsupportedRetractions.removeAll(currentActive.keySet());
    unsupportedRetractions.removeAll(currentRetracted.keySet());
    if (!unsupportedRetractions.isEmpty()) {
      throw new DomainConflictException("only a previously represented measurement result can be retracted");
    }

    validateRetractionContinuity(
        current,
        currentRecords,
        currentActive,
        currentRetracted,
        correction,
        correctedRetracted
    );
  }

  static void requireRetraction(
      @NonNull MeasurementSetRevision current,
      @NonNull MeasurementSetRevision retraction
  ) {
    if (!current.name().equals(retraction.name())
        || !Objects.equals(current.note(), retraction.note())
        || !current.records().equals(retraction.records())
        || !current.resultRetractions().equals(retraction.resultRetractions())) {
      throw new DomainConflictException(
          "full measurement retraction must preserve the current revision evidence and metadata"
      );
    }
  }

  private static void validateRetractionContinuity(
      @NonNull MeasurementSetRevision current,
      @NonNull Map<MeasurementRecordId, MeasurementRecord> currentRecords,
      @NonNull Map<MeasurementCharacteristic, MeasurementRecord> currentActive,
      @NonNull Map<MeasurementCharacteristic, MeasurementResultRetraction> currentRetracted,
      @NonNull MeasurementSetRevision correction,
      @NonNull Map<MeasurementCharacteristic, MeasurementResultRetraction> correctedRetracted
  ) {
    for (final var entry : correctedRetracted.entrySet()) {
      final var characteristic = entry.getKey();
      final var retraction = entry.getValue();
      final var previousRetraction = currentRetracted.get(characteristic);
      if (previousRetraction != null) {
        requireUnchangedRetraction(previousRetraction, retraction);
        continue;
      }
      requireCurrentEvidence(currentRecords, currentActive.get(characteristic), retraction);
      requireSourceRevision(current, retraction);
      requireCorrectionAudit(correction, retraction);
    }
  }

  private static void requireUnchangedRetraction(
      @NonNull MeasurementResultRetraction previous,
      @NonNull MeasurementResultRetraction current
  ) {
    if (!previous.equals(current)) {
      throw new DomainConflictException(
          "carried measurement result retraction must remain immutable"
      );
    }
  }

  private static void requireCurrentEvidence(
      @NonNull Map<MeasurementRecordId, MeasurementRecord> currentRecords,
      @Nullable MeasurementRecord currentResult,
      @NonNull MeasurementResultRetraction retraction
  ) {
    if (currentResult == null || !currentResult.equals(retraction.result())) {
      throw new DomainConflictException(
          "measurement result retraction must preserve the current active result evidence"
      );
    }
    for (final var observation : retraction.observations()) {
      if (!observation.equals(currentRecords.get(observation.recordId()))) {
        throw new DomainConflictException(
            "measurement result retraction must preserve its current observation evidence"
        );
      }
    }
  }

  private static void requireSourceRevision(
      @NonNull MeasurementSetRevision current,
      @NonNull MeasurementResultRetraction retraction
  ) {
    if (!retraction.sourceRevisionId().equals(current.revisionId())
        || !retraction.sourceRevisionNumber().equals(current.revisionNumber())) {
      throw new DomainConflictException(
          "measurement result retraction must reference the current source revision"
      );
    }
  }

  private static void requireCorrectionAudit(
      @NonNull MeasurementSetRevision correction,
      @NonNull MeasurementResultRetraction retraction
  ) {
    if (!retraction.actor().equals(correction.actor())
        || !retraction.retractedAt().equals(correction.createdAt())
        || !retraction.reason().equals(correction.correctionReason())) {
      throw new DomainConflictException(
          "new measurement result retraction audit must match its correction revision"
      );
    }
  }

  private static void validateEvidenceIdentityContinuity(
      @NonNull MeasurementSetRevision current,
      @NonNull MeasurementSetRevision correction
  ) {
    final Map<MeasurementRecordId, MeasurementRecord> currentEvidence = new HashMap<>();
    current.records().forEach(measurementRecord -> currentEvidence.put(
        measurementRecord.recordId(),
        measurementRecord
    ));
    current.resultRetractions().forEach(retraction -> {
      currentEvidence.put(retraction.recordId(), retraction.result());
      retraction.observations().forEach(observation -> currentEvidence.put(
          observation.recordId(),
          observation
      ));
    });
    correction.records().forEach(measurementRecord -> {
      final var previousEvidence = currentEvidence.get(measurementRecord.recordId());
      if (previousEvidence != null && !previousEvidence.equals(measurementRecord)) {
        throw new DomainConflictException(
            "measurement evidence identity cannot be rebound to different evidence"
        );
      }
    });
  }

  private static @NonNull Map<MeasurementRecordId, MeasurementRecord> recordsById(
      @NonNull MeasurementSetRevision revision
  ) {
    final Map<MeasurementRecordId, MeasurementRecord> records = new HashMap<>();
    revision.records().forEach(measurementRecord -> records.put(
        measurementRecord.recordId(),
        measurementRecord
    ));
    return Map.copyOf(records);
  }

  private static void validateRetractedResultReplacement(
      @NonNull Map<MeasurementCharacteristic, MeasurementResultRetraction> currentRetractions,
      @NonNull Map<MeasurementCharacteristic, MeasurementRecord> correctedActiveResults
  ) {
    currentRetractions.forEach((characteristic, retraction) -> {
      final var replacement = correctedActiveResults.get(characteristic);
      if (replacement != null && replacement.recordId().equals(retraction.recordId())) {
        throw new DomainConflictException(
            "retracted measurement result requires replacement evidence with a new identity"
        );
      }
    });
  }

  private static @NonNull Map<MeasurementCharacteristic, MeasurementRecord> activeResults(
      @NonNull MeasurementSetRevision revision
  ) {
    final Map<MeasurementCharacteristic, MeasurementRecord> results = new EnumMap<>(
        MeasurementCharacteristic.class
    );
    revision.records().stream()
        .filter(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
        .forEach(measurementRecord -> results.put(
            measurementRecord.characteristic(),
            measurementRecord
        ));
    return Map.copyOf(results);
  }

  private static @NonNull Map<MeasurementCharacteristic, MeasurementResultRetraction>
      retractionsByCharacteristic(
      @NonNull MeasurementSetRevision revision
  ) {
    final Map<MeasurementCharacteristic, MeasurementResultRetraction> retractions = new EnumMap<>(
        MeasurementCharacteristic.class
    );
    revision.resultRetractions().forEach(retraction -> retractions.put(
        retraction.characteristic(),
        retraction
    ));
    return Map.copyOf(retractions);
  }
}
