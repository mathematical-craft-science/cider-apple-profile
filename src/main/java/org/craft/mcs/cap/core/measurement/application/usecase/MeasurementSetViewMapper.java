package org.craft.mcs.cap.core.measurement.application.usecase;

import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementResultRetractionView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetRetractionView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRetraction;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Maps a persisted measurement aggregate revision to its application read model. */
final class MeasurementSetViewMapper {

  private MeasurementSetViewMapper() {
  }

  static @NonNull MeasurementSetView toView(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    requireNonNull(state, "versioned measurement set cannot be null");
    final var set = state.aggregate();
    final var revision = set.currentRevision();
    return new MeasurementSetView(
        set.measurementSetId(),
        set.workspaceId(),
        set.subject(),
        set.lifecycle(),
        toRetractionView(set.retraction()),
        state.revision().value(),
        revision.revisionId(),
        revision.revisionNumber(),
        revision.materialStateId(),
        revision.name(),
        revision.note(),
        revision.correctionReason(),
        revision.createdAt(),
        revision.actor(),
        revision.records().stream()
            .map(MeasurementSetViewMapper::toRecordView)
            .toList(),
        revision.resultRetractions().stream()
            .map(retraction -> new MeasurementResultRetractionView(
                retraction.sourceRevisionId(),
                retraction.sourceRevisionNumber(),
                toRecordView(retraction.result()),
                retraction.observations().stream()
                    .map(MeasurementSetViewMapper::toRecordView)
                    .toList(),
                retraction.actor(),
                retraction.retractedAt(),
                retraction.reason()
            ))
            .toList(),
        set.createdAt(),
        set.updatedAt()
    );
  }

  private static @NonNull MeasurementRecordView toRecordView(
      @NonNull MeasurementRecord measurementRecord
  ) {
    return new MeasurementRecordView(
        measurementRecord.recordId(),
        measurementRecord.characteristic(),
        measurementRecord.role(),
        measurementRecord.value(),
        measurementRecord.measuredAt(),
        measurementRecord.provenance(),
        measurementRecord.derivationMethod(),
        measurementRecord.derivationInputs()
    );
  }

  private static @Nullable MeasurementSetRetractionView toRetractionView(
      @Nullable MeasurementSetRetraction retraction
  ) {
    return retraction == null
        ? null
        : new MeasurementSetRetractionView(
            retraction.actor(),
            retraction.retractedAt(),
            retraction.reason()
        );
  }
}
