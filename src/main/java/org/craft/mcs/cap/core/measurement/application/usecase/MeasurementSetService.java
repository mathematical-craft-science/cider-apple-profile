package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetNotFoundException;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.in.CorrectMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.PatchMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetsUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.ReplaceMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementActorProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Transactional application service for recording, correcting, querying, and retracting measurements.
 */
@Component
@RequiredArgsConstructor
public final class MeasurementSetService
    implements CreateMeasurementSetUseCase,
    CorrectMeasurementSetUseCase,
    QueryMeasurementSetsUseCase,
    RetractMeasurementSetUseCase {

  private final @NonNull MeasurementSetRepository repository;
  private final @NonNull MeasurementSetViewQuery viewQuery;
  private final @NonNull MeasurementSubjectAccessControl subjectAccessControl;
  private final @NonNull MeasurementActorProvider actorProvider;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull MeasurementSetView create(@NonNull CreateMeasurementSetCommand command) {
    requireNonNull(command, "create measurement set command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var workspaceId = subjectAccessControl.requireWriteAccess(command.subject());
      final var now = clock.instant();
      final var revision = revision(
          MeasurementSetRevisionNumber.initial(),
          command.name(),
          command.note(),
          null,
          records(command.measurements(), now),
          now
      );
      final var set = MeasurementSet.create(
          command.requestedId() == null ? MeasurementSetId.newId() : command.requestedId(),
          workspaceId,
          command.subject(),
          revision,
          now
      );
      return toView(repository.create(set));
    });
  }

  @Override
  public @NonNull MeasurementSetView replace(@NonNull ReplaceMeasurementSetCommand command) {
    requireNonNull(command, "replace measurement set command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var state = requireForWrite(command.measurementSetId(), command.expectedVersion());
      final var set = state.aggregate();
      final var now = clock.instant();
      set.correct(
          revision(
              set.currentRevision().revisionNumber().next(),
              command.name(),
              command.note(),
              command.correctionReason(),
              records(command.measurements(), now),
              now
          ),
          now
      );
      return toView(repository.appendRevision(state.withAggregate(set)));
    });
  }

  @Override
  public @NonNull MeasurementSetView patch(@NonNull PatchMeasurementSetCommand command) {
    requireNonNull(command, "patch measurement set command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var state = requireForWrite(command.measurementSetId(), command.expectedVersion());
      final var set = state.aggregate();
      final var current = set.currentRevision();
      final var now = clock.instant();
      final var replacementRecords = records(command.replacements(), now);
      final var removed = command.removals();
      final var replaced = replacementRecords.stream()
          .map(MeasurementRecord::characteristic)
          .collect(Collectors.toUnmodifiableSet());
      final var copied = copyUnchanged(current.records(), replaced, removed);
      final var nextRecords = new ArrayList<>(copied);
      nextRecords.addAll(replacementRecords);
      set.correct(
          revision(
              current.revisionNumber().next(),
              command.name() == null ? current.name() : command.name(),
              command.noteSpecified() ? command.note() : current.note(),
              command.correctionReason(),
              nextRecords,
              now
          ),
          now
      );
      return toView(repository.appendRevision(state.withAggregate(set)));
    });
  }

  @Override
  public @NonNull MeasurementSetView get(@NonNull MeasurementSetId measurementSetId) {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    final var view = viewQuery.findById(measurementSetId)
        .orElseThrow(() -> new MeasurementSetNotFoundException(measurementSetId));
    subjectAccessControl.requireReadAccess(view.subject());
    return view;
  }

  @Override
  public @NonNull List<@NonNull MeasurementSetView> list(
      @NonNull MeasurementSubjectReference subject
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    final var workspaceId = subjectAccessControl.requireReadAccess(subject);
    return viewQuery.findBySubject(workspaceId, subject);
  }

  @Override
  public void retract(@NonNull RetractMeasurementSetCommand command) {
    requireNonNull(command, "retract measurement set command cannot be null");
    transactionHelper.executeInTransaction(() -> {
      final var state = requireForWrite(command.measurementSetId(), command.expectedVersion());
      state.aggregate().retract(clock.instant());
      repository.retract(state);
    });
  }

  private @NonNull VersionedAggregate<MeasurementSet> requireForWrite(
      @NonNull MeasurementSetId measurementSetId,
      long expectedVersion
  ) {
    final var state = requireState(measurementSetId);
    if (state.revision().value() != expectedVersion) {
      throw new ConcurrentResourceModificationException("measurement set", null);
    }
    subjectAccessControl.requireWriteAccess(state.aggregate().subject());
    return state;
  }

  private @NonNull VersionedAggregate<MeasurementSet> requireState(
      @NonNull MeasurementSetId measurementSetId
  ) {
    return repository.findVersionedById(measurementSetId)
        .orElseThrow(() -> new MeasurementSetNotFoundException(measurementSetId));
  }

  private @NonNull List<@NonNull MeasurementRecord> records(
      @NonNull List<@NonNull MeasurementCharacteristicInput> inputs,
      @NonNull Instant recordedAt
  ) {
    final var records = new MeasurementRecordFactory().create(inputs);
    if (records.stream().anyMatch(r -> r.measuredAt().isAfter(recordedAt))) {
      throw new ApplicationValidationException("measurement time cannot be in the future");
    }
    return records;
  }

  private @NonNull MeasurementSetRevision revision(
      @NonNull MeasurementSetRevisionNumber revisionNumber,
      @NonNull MeasurementSetName name,
      @Nullable String note,
      @Nullable MeasurementCorrectionReason correctionReason,
      @NonNull List<@NonNull MeasurementRecord> records,
      @NonNull Instant createdAt
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        revisionNumber,
        name,
        note,
        correctionReason,
        createdAt,
        actorProvider.requireCurrentActorReference(),
        records
    );
  }

  private static @NonNull List<@NonNull MeasurementRecord> copyUnchanged(
      @NonNull List<@NonNull MeasurementRecord> current,
      @NonNull Set<@NonNull MeasurementCharacteristic> replaced,
      @NonNull Set<@NonNull MeasurementCharacteristic> removed
  ) {
    final var retained = current.stream()
        .filter(r -> !replaced.contains(r.characteristic()))
        .filter(r -> !removed.contains(r.characteristic()))
        .toList();
    final Map<MeasurementRecordId, MeasurementRecordId> copiedIds = new HashMap<>();
    retained.forEach(r -> copiedIds.put(r.recordId(), MeasurementRecordId.newId()));
    return retained.stream()
        .map(r -> new MeasurementRecord(
            copiedIds.get(r.recordId()),
            r.characteristic(),
            r.role(),
            r.value(),
            r.measuredAt(),
            r.provenance(),
            r.derivationMethod(),
            r.derivationInputs().stream()
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
        "copied measurement derivation input id cannot be null"
    );
  }

  private static @NonNull MeasurementSetView toView(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    final var set = state.aggregate();
    final var revision = set.currentRevision();
    return new MeasurementSetView(
        set.measurementSetId(),
        set.workspaceId(),
        set.subject(),
        set.lifecycle(),
        state.revision().value(),
        revision.revisionId(),
        revision.revisionNumber(),
        revision.name(),
        revision.note(),
        revision.correctionReason(),
        revision.createdAt(),
        revision.actor(),
        revision.records().stream()
            .map(r -> new MeasurementRecordView(
                r.recordId(),
                r.characteristic(),
                r.role(),
                r.value(),
                r.measuredAt(),
                r.provenance(),
                r.derivationMethod(),
                r.derivationInputs()
            ))
            .toList(),
        set.createdAt(),
        set.updatedAt()
    );
  }
}
