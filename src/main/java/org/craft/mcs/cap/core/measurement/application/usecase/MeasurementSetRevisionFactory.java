package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.PatchMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.ReplaceMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementResultRetraction;
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
            records(command.measurements()),
            List.of()
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
    final var replacementRecords = records(command.measurements());
    final var replaced = replacementRecords.stream()
        .map(MeasurementRecord::characteristic)
        .collect(Collectors.toUnmodifiableSet());
    final var removed = current.records().stream()
        .map(MeasurementRecord::characteristic)
        .filter(characteristic -> !replaced.contains(characteristic))
        .collect(Collectors.toUnmodifiableSet());
    return revision(
        current.revisionNumber().next(),
        current.materialStateId(),
        new RevisionContent(
            command.name(),
            command.note(),
            command.correctionReason(),
            replacementRecords,
            resultRetractions(
                current,
                replaced,
                removed,
                actor,
                createdAt,
                command.correctionReason()
            )
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
    final var replacementRecords = records(command.replacements());
    final var replaced = replacementRecords.stream()
        .map(MeasurementRecord::characteristic)
        .collect(Collectors.toUnmodifiableSet());
    final var nextRecords = new ArrayList<>(copyUnchanged(
        current.records(),
        replaced,
        command.removals()
    ));
    nextRecords.addAll(replacementRecords);
    final var nextRetractions = resultRetractions(
        current,
        replaced,
        command.removals(),
        actor,
        createdAt,
        command.correctionReason()
    );
    return revision(
        current.revisionNumber().next(),
        current.materialStateId(),
        new RevisionContent( // NOSONAR
            command.name() == null ? current.name() : command.name(),
            command.noteSpecified() ? command.note() : current.note(),
            command.correctionReason(),
            nextRecords,
            nextRetractions
        ),
        createdAt,
        actor
    );
  }

  private @NonNull List<@NonNull MeasurementRecord> records(
      @NonNull List<@NonNull MeasurementCharacteristicInput> inputs
  ) {
    return recordFactory.create(inputs);
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
        content.records(),
        content.resultRetractions()
    );
  }

  private static @NonNull List<@NonNull MeasurementRecord> copyUnchanged(
      @NonNull List<@NonNull MeasurementRecord> current,
      @NonNull Set<@NonNull MeasurementCharacteristic> replaced,
      @NonNull Set<@NonNull MeasurementCharacteristic> removed
  ) {
    return current.stream()
        .filter(measurementRecord -> !replaced.contains(measurementRecord.characteristic()))
        .filter(measurementRecord -> !removed.contains(measurementRecord.characteristic()))
        .toList();
  }

  private static @NonNull List<@NonNull MeasurementResultRetraction> resultRetractions(
      @NonNull MeasurementSetRevision current,
      @NonNull Set<@NonNull MeasurementCharacteristic> replaced,
      @NonNull Set<@NonNull MeasurementCharacteristic> removed,
      @NonNull ActorReference actor,
      @NonNull Instant retractedAt,
      @NonNull MeasurementCorrectionReason reason
  ) {
    final var activeResults = current.records().stream()
        .filter(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
        .collect(Collectors.toUnmodifiableMap(
            MeasurementRecord::characteristic,
            Function.identity()
        ));
    final var observationsByCharacteristic = current.records().stream()
        .filter(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.OBSERVATION)
        .collect(Collectors.groupingBy(MeasurementRecord::characteristic));
    if (!activeResults.keySet().containsAll(removed)) {
      throw new ApplicationValidationException(
          "only an active measurement result can be retracted"
      );
    }

    final var retractions = new ArrayList<MeasurementResultRetraction>();
    current.resultRetractions().stream()
        .filter(retraction -> !replaced.contains(retraction.characteristic()))
        .forEach(retractions::add);
    removed.stream()
        .sorted()
        .map(activeResults::get)
        .map(result -> new MeasurementResultRetraction(
            current.revisionId(),
            current.revisionNumber(),
            result,
            observationsByCharacteristic.getOrDefault(result.characteristic(), List.of()),
            actor,
            retractedAt,
            reason
        ))
        .forEach(retractions::add);
    return List.copyOf(retractions);
  }

  private record RevisionContent(
      @NonNull MeasurementSetName name,
      @Nullable String note,
      @Nullable MeasurementCorrectionReason correctionReason,
      @NonNull List<@NonNull MeasurementRecord> records,
      @NonNull List<@NonNull MeasurementResultRetraction> resultRetractions
  ) {

    private RevisionContent {
      requireNonNull(name, "measurement revision name cannot be null");
      records = List.copyOf(requireNonNull(records, "measurement revision records cannot be null"));
      resultRetractions = List.copyOf(requireNonNull(
          resultRetractions,
          "measurement revision retractions cannot be null"
      ));
    }
  }
}
