package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.PatchMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.ReplaceMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Creates immutable measurement revisions for initial recording and later corrections. */
@Component
final class MeasurementSetRevisionFactory {

  private final @NonNull MeasurementRecordFactory recordFactory = new MeasurementRecordFactory();

  @NonNull MeasurementSetRevision initial(
      @NonNull CreateMeasurementSetCommand command,
      @NonNull MaterialStateId materialStateId,
      @NonNull Instant createdAt,
      @NonNull ActorReference actor
  ) {
    requireNonNull(command, "create measurement set command cannot be null");
    return revision(
        MeasurementSetRevisionNumber.initial(),
        materialStateId,
        new RevisionContent(
            command.name(),
            command.note(),
            null,
            records(command.measurements(), createdAt)
        ),
        createdAt,
        actor
    );
  }

  @NonNull MeasurementSetRevision replacement(
      @NonNull MeasurementSetRevision current,
      @NonNull ReplaceMeasurementSetCommand command,
      @NonNull Instant createdAt,
      @NonNull ActorReference actor
  ) {
    requireNonNull(current, "current measurement revision cannot be null");
    requireNonNull(command, "replace measurement set command cannot be null");
    return revision(
        current.revisionNumber().next(),
        current.materialStateId(),
        new RevisionContent(
            command.name(),
            command.note(),
            command.correctionReason(),
            records(command.measurements(), createdAt)
        ),
        createdAt,
        actor
    );
  }

  @NonNull MeasurementSetRevision patch(
      @NonNull MeasurementSetRevision current,
      @NonNull PatchMeasurementSetCommand command,
      @NonNull Instant createdAt,
      @NonNull ActorReference actor
  ) {
    requireNonNull(current, "current measurement revision cannot be null");
    requireNonNull(command, "patch measurement set command cannot be null");
    final var replacementRecords = records(command.replacements(), createdAt);
    final var replaced = replacementRecords.stream()
        .map(MeasurementRecord::characteristic)
        .collect(Collectors.toUnmodifiableSet());
    final var nextRecords = new ArrayList<>(copyUnchanged(
        current.records(),
        replaced,
        command.removals()
    ));
    nextRecords.addAll(replacementRecords);
    return revision(
        current.revisionNumber().next(),
        current.materialStateId(),
        new RevisionContent( // NOSONAR
            command.name() == null ? current.name() : command.name(),
            command.noteSpecified() ? command.note() : current.note(),
            command.correctionReason(),
            nextRecords
        ),
        createdAt,
        actor
    );
  }

  private @NonNull List<@NonNull MeasurementRecord> records(
      @NonNull List<@NonNull MeasurementCharacteristicInput> inputs,
      @NonNull Instant recordedAt
  ) {
    final var records = recordFactory.create(inputs);
    if (records.stream().anyMatch(measurementRecord -> measurementRecord.measuredAt().isAfter(recordedAt))) {
      throw new ApplicationValidationException("measurement time cannot be in the future");
    }
    return records;
  }

  private static @NonNull MeasurementSetRevision revision(
      @NonNull MeasurementSetRevisionNumber revisionNumber,
      @NonNull MaterialStateId materialStateId,
      @NonNull RevisionContent content,
      @NonNull Instant createdAt,
      @NonNull ActorReference actor
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        revisionNumber,
        materialStateId,
        content.name(),
        content.note(),
        content.correctionReason(),
        createdAt,
        actor,
        content.records()
    );
  }

  private static @NonNull List<@NonNull MeasurementRecord> copyUnchanged(
      @NonNull List<@NonNull MeasurementRecord> current,
      @NonNull Set<@NonNull MeasurementCharacteristic> replaced,
      @NonNull Set<@NonNull MeasurementCharacteristic> removed
  ) {
    final var retained = current.stream()
        .filter(measurementRecord -> !replaced.contains(measurementRecord.characteristic()))
        .filter(measurementRecord -> !removed.contains(measurementRecord.characteristic()))
        .toList();
    final Map<MeasurementRecordId, MeasurementRecordId> copiedIds = new HashMap<>();
    retained.forEach(measurementRecord ->
        copiedIds.put(measurementRecord.recordId(), MeasurementRecordId.newId())
    );
    return retained.stream()
        .map(measurementRecord -> new MeasurementRecord(
            copiedRecordId(copiedIds, measurementRecord.recordId()),
            measurementRecord.characteristic(),
            measurementRecord.role(),
            measurementRecord.value(),
            measurementRecord.measuredAt(),
            measurementRecord.provenance(),
            measurementRecord.derivationMethod(),
            measurementRecord.derivationInputs().stream()
                .map(inputId -> copiedRecordId(copiedIds, inputId))
                .toList()
        ))
        .toList();
  }

  private static @NonNull MeasurementRecordId copiedRecordId(
      @NonNull Map<@NonNull MeasurementRecordId, @NonNull MeasurementRecordId> copiedIds,
      @NonNull MeasurementRecordId originalId
  ) {
    return requireNonNull(
        copiedIds.get(originalId),
        "copied measurement record id cannot be null"
    );
  }

  private record RevisionContent(
      @NonNull MeasurementSetName name,
      @Nullable String note,
      @Nullable MeasurementCorrectionReason correctionReason,
      @NonNull List<@NonNull MeasurementRecord> records
  ) {

    private RevisionContent {
      requireNonNull(name, "measurement revision name cannot be null");
      records = List.copyOf(requireNonNull(records, "measurement revision records cannot be null"));
    }
  }
}
