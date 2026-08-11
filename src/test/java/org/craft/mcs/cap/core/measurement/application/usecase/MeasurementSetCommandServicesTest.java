package org.craft.mcs.cap.core.measurement.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.PatchMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.ReplaceMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementActorProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.ObservedMaterialStateValidator;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectChangePolicy;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementResultRetraction;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionChangeKind;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRetraction;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.pointValue;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;


class MeasurementSetCommandServicesTest {

  private static final Instant INITIAL_TIME = Instant.parse("2026-07-20T10:00:00Z");
  private static final Instant CORRECTION_TIME = Instant.parse("2026-07-24T10:00:00Z");
  private static final MeasurementProvenance PROVENANCE = new MeasurementProvenance(
      MeasurementSourceType.USER,
      MeasurementSourceName.of("Manual entry"),
      null,
      null,
      null
  );

  @Test
  void createAttachesEvidenceToObservedMaterialState() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var materialStateValidator = mock(ObservedMaterialStateValidator.class);
    final var workspaceId = WorkspaceId.newId();
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var observedStateId = MaterialStateId.newId();
    when(subjectAccessControl.requireWriteAccess(subject)).thenReturn(workspaceId);
    when(materialStateValidator.requireObservedState(subject, observedStateId)).thenReturn(observedStateId);
    when(repository.create(any())).thenAnswer(invocation -> new VersionedAggregate<>(
        invocation.getArgument(0),
        AggregateRevision.of(0)
    ));
    final var service = createService(
        repository,
        subjectAccessControl,
        materialStateValidator
    );

    final var result = service.create(command(subject, observedStateId));

    assertThat(result)
        .returns(subject, MeasurementSetView::subject)
        .returns(observedStateId, MeasurementSetView::materialStateId)
        .satisfies(view -> assertThat(view.records()).hasSize(1));
    verify(subjectAccessControl).requireWriteAccess(subject);
  }

  @Test
  void createRecordsMultipleLaboratoryCharacteristicsInOneMeasurementSet() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var materialStateValidator = mock(ObservedMaterialStateValidator.class);
    final var workspaceId = WorkspaceId.newId();
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var observedStateId = MaterialStateId.newId();
    when(subjectAccessControl.requireWriteAccess(subject)).thenReturn(workspaceId);
    when(materialStateValidator.requireObservedState(subject, observedStateId)).thenReturn(observedStateId);
    when(repository.create(any())).thenAnswer(invocation -> new VersionedAggregate<>(
        invocation.getArgument(0),
        AggregateRevision.of(0)
    ));
    final var service = createService(repository, subjectAccessControl, materialStateValidator);
    final var command = new CreateMeasurementSetCommand(
        null,
        subject,
        observedStateId,
        MeasurementSetName.of("Laboratory panel"),
        null,
        List.of(
            direct(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052", CORRECTION_TIME),
            direct(MeasurementCharacteristic.PH, "3.35", CORRECTION_TIME),
            direct(MeasurementCharacteristic.TITRATABLE_ACIDITY, "6.20", CORRECTION_TIME)
        )
    );

    final var result = service.create(command);

    assertThat(result)
        .returns(observedStateId, MeasurementSetView::materialStateId)
        .satisfies(view -> assertThat(view.records())
            .extracting(MeasurementRecordView::characteristic)
            .containsExactlyInAnyOrder(
                MeasurementCharacteristic.SPECIFIC_GRAVITY,
                MeasurementCharacteristic.PH,
                MeasurementCharacteristic.TITRATABLE_ACIDITY
            ));
  }

  @Test
  void createRejectsStateThatDoesNotBelongToSubjectBeforePersistingEvidence() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var materialStateValidator = mock(ObservedMaterialStateValidator.class);
    final var workspaceId = WorkspaceId.newId();
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var observedStateId = MaterialStateId.newId();
    when(subjectAccessControl.requireWriteAccess(subject)).thenReturn(workspaceId);
    when(materialStateValidator.requireObservedState(subject, observedStateId))
        .thenThrow(new ApplicationValidationException(
            "observed material state does not belong to the measurement subject"
        ));
    final var service = createService(
        repository,
        subjectAccessControl,
        materialStateValidator
    );

    assertThatThrownBy(() -> service.create(command(subject, observedStateId)))
        .isInstanceOf(ApplicationValidationException.class);
    verify(repository, never()).create(any());
  }

  @Test
  void createRejectsMeasurementWriteWhenSubjectLifecycleDisallowsChanges() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var subjectChangePolicy = mock(MeasurementSubjectChangePolicy.class);
    final var materialStateValidator = mock(ObservedMaterialStateValidator.class);
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var observedStateId = MaterialStateId.newId();
    when(subjectAccessControl.requireWriteAccess(subject)).thenReturn(WorkspaceId.newId());
    doThrow(new DomainConflictException(
        "feedstock batch measurements can be changed only when lifecycle is ACTIVE"
    )).when(subjectChangePolicy).requireChangesAllowed(subject);
    final var service = createService(
        repository,
        subjectAccessControl,
        subjectChangePolicy,
        materialStateValidator
    );

    assertThatThrownBy(() -> service.create(command(subject, observedStateId)))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("feedstock batch measurements can be changed only when lifecycle is ACTIVE");
    verify(repository, never()).create(any());
    verify(materialStateValidator, never()).requireObservedState(any(), any());
  }

  @Test
  void patchAppendsRevisionAndPreservesUnchangedEvidenceIdentity() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var set = measurementSet();
    final var originalSugar = set.currentRevision().records().getFirst();
    final var state = new VersionedAggregate<>(set, AggregateRevision.of(3));
    when(repository.findVersionedById(set.measurementSetId())).thenReturn(Optional.of(state));
    when(repository.appendRevision(any())).thenAnswer(invocation -> {
      final VersionedAggregate<MeasurementSet> argument = invocation.getArgument(0);
      return new VersionedAggregate<>(argument.aggregate(), AggregateRevision.of(4));
    });
    final var service = correctionService(repository, subjectAccessControl);

    final var result = service.patch(new PatchMeasurementSetCommand(
        set.measurementSetId(),
        3,
        null,
        false,
        null,
        MeasurementCorrectionReason.of("Corrected pH"),
        List.of(direct(MeasurementCharacteristic.PH, "3.25", CORRECTION_TIME)),
        Set.of()
    ));

    assertThat(result)
        .returns(4L, MeasurementSetView::version)
        .returns(MeasurementSetRevisionNumber.of(2), MeasurementSetView::revisionNumber)
        .satisfies(view -> assertThat(view.records())
            .hasSize(2)
            .anySatisfy(recordView -> assertThat(recordView)
                .returns(MeasurementCharacteristic.SPECIFIC_GRAVITY, MeasurementRecordView::characteristic)
                .returns(INITIAL_TIME, MeasurementRecordView::measuredAt)
                .returns(originalSugar.recordId(), MeasurementRecordView::recordId))
            .anySatisfy(recordView -> assertThat(recordView)
                .returns(MeasurementCharacteristic.PH, MeasurementRecordView::characteristic)
                .returns(CORRECTION_TIME, MeasurementRecordView::measuredAt)
                .satisfies(value -> assertThat(pointValue(value.value()).asBigDecimal())
                    .isEqualByComparingTo("3.25"))));
    verify(subjectAccessControl).requireWriteAccess(set.subject());
  }

  @Test
  void patchRetractionIsRetainedUntilTheCharacteristicIsReplaced() {
    final var set = measurementSet();
    final var actor = ActorReference.accountUser(newUuid());
    final var factory = new MeasurementSetRevisionFactory();
    final var reason = MeasurementCorrectionReason.of("Laboratory invalidated pH result");
    final var retracted = factory.patch(
        set.currentRevision(),
        patchCommand(set, reason, List.of(), Set.of(MeasurementCharacteristic.PH)),
        CORRECTION_TIME,
        actor
    );

    assertThat(retracted)
        .satisfies(revision -> assertThat(revision.records())
            .singleElement()
            .returns(MeasurementCharacteristic.SPECIFIC_GRAVITY, MeasurementRecord::characteristic))
        .satisfies(revision -> assertThat(revision.resultRetractions())
            .singleElement()
            .returns(MeasurementCharacteristic.PH, MeasurementResultRetraction::characteristic)
            .returns(actor, MeasurementResultRetraction::actor)
            .returns(CORRECTION_TIME, MeasurementResultRetraction::retractedAt)
            .returns(reason, MeasurementResultRetraction::reason));

    final var carried = factory.patch(
        retracted,
        new PatchMeasurementSetCommand(
            set.measurementSetId(),
            0,
            null,
            true,
            "Clarified note",
            MeasurementCorrectionReason.of("Clarified the note"),
            List.of(),
            Set.of()
        ),
        CORRECTION_TIME.plusSeconds(60),
        ActorReference.accountUser(newUuid())
    );
    assertThat(carried.resultRetractions()).containsExactlyElementsOf(retracted.resultRetractions());

    final var restored = factory.patch(
        carried,
        patchCommand(
            set,
            MeasurementCorrectionReason.of("Recorded replacement pH result"),
            List.of(direct(MeasurementCharacteristic.PH, "3.30", CORRECTION_TIME)),
            Set.of()
        ),
        CORRECTION_TIME.plusSeconds(120),
        ActorReference.accountUser(newUuid())
    );
    assertThat(restored)
        .satisfies(revision -> assertThat(revision.resultRetractions()).isEmpty())
        .satisfies(revision -> assertThat(revision.records())
            .extracting(MeasurementRecord::characteristic)
            .containsExactlyInAnyOrder(
                MeasurementCharacteristic.SPECIFIC_GRAVITY,
                MeasurementCharacteristic.PH
            ));

    final var repeatedRetraction = patchCommand(
        set,
        MeasurementCorrectionReason.of("Repeated retraction"),
        List.of(),
        Set.of(MeasurementCharacteristic.PH)
    );
    assertThatThrownBy(() -> factory.patch(
        retracted,
        repeatedRetraction,
        CORRECTION_TIME.plusSeconds(60),
        actor
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("only an active measurement result can be retracted");
  }

  @Test
  void fullReplacementCreatesTombstoneForAnOmittedActiveResult() {
    final var set = measurementSet();
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Replaced the complete laboratory result");
    final var replacement = new ReplaceMeasurementSetCommand(
        set.measurementSetId(),
        0,
        MeasurementSetName.of("Replacement result"),
        null,
        reason,
        List.of(direct(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.051", CORRECTION_TIME))
    );

    final var corrected = new MeasurementSetRevisionFactory().replacement(
        set.currentRevision(),
        replacement,
        CORRECTION_TIME,
        actor
    );
    set.correct(corrected);

    assertThat(corrected)
        .satisfies(revision -> assertThat(revision.records())
            .singleElement()
            .returns(MeasurementCharacteristic.SPECIFIC_GRAVITY, MeasurementRecord::characteristic))
        .satisfies(revision -> assertThat(revision.resultRetractions())
            .singleElement()
            .returns(MeasurementCharacteristic.PH, MeasurementResultRetraction::characteristic)
            .returns(actor, MeasurementResultRetraction::actor)
            .returns(reason, MeasurementResultRetraction::reason));
  }

  @Test
  void fullReplacementChangesEveryResultWithoutCreatingRetractions() {
    final var set = measurementSet();
    final var previousRecordIds = set.currentRevision().records().stream()
        .map(MeasurementRecord::recordId)
        .toList();
    final var replacement = new ReplaceMeasurementSetCommand(
        set.measurementSetId(),
        0,
        MeasurementSetName.of("Corrected laboratory panel"),
        "Both values were transcribed incorrectly",
        MeasurementCorrectionReason.of("Corrected complete laboratory report"),
        List.of(
            direct(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.049", CORRECTION_TIME),
            direct(MeasurementCharacteristic.PH, "3.30", CORRECTION_TIME)
        )
    );

    final var corrected = new MeasurementSetRevisionFactory().replacement(
        set.currentRevision(),
        replacement,
        CORRECTION_TIME,
        ActorReference.accountUser(newUuid())
    );
    set.correct(corrected);

    assertThat(corrected)
        .satisfies(revision -> assertThat(revision.resultRetractions()).isEmpty())
        .satisfies(revision -> assertThat(revision.records())
            .hasSize(2)
            .extracting(MeasurementRecord::recordId)
            .doesNotContainAnyElementsOf(previousRecordIds))
        .satisfies(revision -> assertThat(revision.records())
            .extracting(MeasurementRecord::characteristic)
            .containsExactlyInAnyOrder(
                MeasurementCharacteristic.SPECIFIC_GRAVITY,
                MeasurementCharacteristic.PH
            ));
  }

  @Test
  void separateAxisCorrectionsComposeIntoOneCurrentRevision() {
    final var set = measurementSet();
    final var factory = new MeasurementSetRevisionFactory();
    final var correctedSugar = factory.patch(
        set.currentRevision(),
        patchCommand(
            set,
            MeasurementCorrectionReason.of("Corrected SG transcription"),
            List.of(direct(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.049", CORRECTION_TIME)),
            Set.of()
        ),
        CORRECTION_TIME,
        ActorReference.accountUser(newUuid())
    );
    set.correct(correctedSugar);
    final var retractionTime = CORRECTION_TIME.plusSeconds(60);
    final var retractedPh = factory.patch(
        set.currentRevision(),
        patchCommand(
            set,
            MeasurementCorrectionReason.of("Laboratory invalidated pH"),
            List.of(),
            Set.of(MeasurementCharacteristic.PH)
        ),
        retractionTime,
        ActorReference.accountUser(newUuid())
    );
    set.correct(retractedPh);

    assertThat(set.currentRevision())
        .returns(MeasurementSetRevisionNumber.of(3), MeasurementSetRevision::revisionNumber)
        .satisfies(revision -> assertThat(revision.records())
            .singleElement()
            .returns(
                MeasurementCharacteristic.SPECIFIC_GRAVITY,
                MeasurementRecord::characteristic
            )
            .satisfies(result -> assertThat(pointValue(result.value()).asBigDecimal())
                .isEqualByComparingTo("1.049")))
        .satisfies(revision -> assertThat(revision.resultRetractions())
            .singleElement()
            .returns(MeasurementCharacteristic.PH, MeasurementResultRetraction::characteristic));
  }

  @Test
  void patchRejectsStaleExpectedVersionBeforeAppendingRevision() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var set = measurementSet();
    when(repository.findVersionedById(set.measurementSetId())).thenReturn(Optional.of(
        new VersionedAggregate<>(set, AggregateRevision.of(4))
    ));
    final var service = correctionService(repository, subjectAccessControl);
    final var command = new PatchMeasurementSetCommand(
        set.measurementSetId(),
        3,
        null,
        false,
        null,
        MeasurementCorrectionReason.of("Stale correction"),
        List.of(direct(MeasurementCharacteristic.PH, "3.25", CORRECTION_TIME)),
        Set.of()
    );

    assertThatThrownBy(() -> service.patch(command))
        .isInstanceOf(ConcurrentResourceModificationException.class);
    verify(subjectAccessControl).requireWriteAccess(set.subject());
    verify(repository, never()).appendRevision(any());
  }

  @Test
  void patchRejectsMeasurementWriteWhenSubjectLifecycleDisallowsChanges() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var subjectChangePolicy = mock(MeasurementSubjectChangePolicy.class);
    final var set = measurementSet();
    when(repository.findVersionedById(set.measurementSetId())).thenReturn(Optional.of(
        new VersionedAggregate<>(set, AggregateRevision.of(3))
    ));
    doThrow(new DomainConflictException(
        "feedstock batch measurements can be changed only when lifecycle is ACTIVE"
    )).when(subjectChangePolicy).requireChangesAllowed(set.subject());
    final var service = correctionService(repository, subjectAccessControl, subjectChangePolicy);

    assertThatThrownBy(() -> service.patch(new PatchMeasurementSetCommand(
        set.measurementSetId(),
        3,
        null,
        false,
        null,
        MeasurementCorrectionReason.of("Corrected pH"),
        List.of(direct(MeasurementCharacteristic.PH, "3.25", CORRECTION_TIME)),
        Set.of()
    )))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("feedstock batch measurements can be changed only when lifecycle is ACTIVE");
    verify(repository, never()).appendRevision(any());
  }

  @Test
  void replaceRejectsMeasurementWriteWhenSubjectLifecycleDisallowsChanges() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var subjectChangePolicy = mock(MeasurementSubjectChangePolicy.class);
    final var set = measurementSet();
    when(repository.findVersionedById(set.measurementSetId())).thenReturn(Optional.of(
        new VersionedAggregate<>(set, AggregateRevision.of(3))
    ));
    doThrow(new DomainConflictException(
        "feedstock batch measurements can be changed only when lifecycle is ACTIVE"
    )).when(subjectChangePolicy).requireChangesAllowed(set.subject());
    final var service = correctionService(repository, subjectAccessControl, subjectChangePolicy);

    assertThatThrownBy(() -> service.replace(new ReplaceMeasurementSetCommand(
        set.measurementSetId(),
        3,
        MeasurementSetName.of("Corrected laboratory result"),
        null,
        MeasurementCorrectionReason.of("Corrected complete laboratory report"),
        List.of(direct(MeasurementCharacteristic.PH, "3.25", CORRECTION_TIME))
    )))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("feedstock batch measurements can be changed only when lifecycle is ACTIVE");
    verify(repository, never()).appendRevision(any());
  }

  @Test
  void rejectsNegativeExpectedVersionBeforeCallingPersistence() {
    final var measurementSetId = MeasurementSetId.newId();

    assertThatThrownBy(() -> new RetractMeasurementSetCommand(
        measurementSetId,
        -1,
        MeasurementRetractionReason.of("Invalidated measurement set")
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("expected measurement-set version cannot be negative");
  }

  @Test
  void patchRejectsReplacingAndRemovingSameCharacteristic() {
    final var measurementSetId = MeasurementSetId.newId();

    assertThatThrownBy(() -> new PatchMeasurementSetCommand(
        measurementSetId,
        0,
        null,
        false,
        null,
        MeasurementCorrectionReason.of("Conflicting correction"),
        List.of(direct(MeasurementCharacteristic.PH, "3.25", CORRECTION_TIME)),
        Set.of(MeasurementCharacteristic.PH)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("measurement characteristic cannot be replaced and removed in the same correction");
  }

  @Test
  void patchRequiresAtLeastOneChange() {
    assertThatThrownBy(() -> new PatchMeasurementSetCommand(
        MeasurementSetId.newId(),
        0,
        null,
        false,
        null,
        MeasurementCorrectionReason.of("No-op correction"),
        List.of(),
        Set.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("measurement patch requires at least one change");
  }

  @Test
  void fullReplacementRequiresAtLeastOneCharacteristic() {
    assertThatThrownBy(() -> new ReplaceMeasurementSetCommand(
        MeasurementSetId.newId(),
        0,
        MeasurementSetName.of("Empty replacement"),
        null,
        MeasurementCorrectionReason.of("Invalid replacement"),
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("measurement replacement requires at least one characteristic");
  }

  @Test
  void retractPreservesEvidenceAndChangesAggregateLifecycle() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var set = measurementSet();
    final var state = new VersionedAggregate<>(set, AggregateRevision.of(2));
    when(repository.findVersionedById(set.measurementSetId())).thenReturn(Optional.of(state));
    when(repository.appendRevision(state)).thenReturn(state);
    final var service = retractionService(repository, subjectAccessControl);

    final var reason = MeasurementRetractionReason.of("Invalidated measurement set");

    service.retract(new RetractMeasurementSetCommand(set.measurementSetId(), 2, reason));

    assertThat(set)
        .returns(MeasurementSetLifecycle.RETRACTED, MeasurementSet::lifecycle)
        .satisfies(retracted -> assertThat(retracted.currentRevision().records()).hasSize(2))
        .satisfies(retracted -> assertThat(retracted.currentRevision())
            .returns(MeasurementSetRevisionChangeKind.RETRACTION, MeasurementSetRevision::changeKind)
            .returns(reason, MeasurementSetRevision::retractionReason))
        .satisfies(retracted -> assertThat(retracted.retraction())
            .isNotNull()
            .returns(CORRECTION_TIME, MeasurementSetRetraction::retractedAt)
            .returns(reason, MeasurementSetRetraction::reason));
    verify(subjectAccessControl).requireWriteAccess(set.subject());
    verify(repository).appendRevision(state);
  }

  @Test
  void retractRejectsMeasurementWriteWhenSubjectLifecycleDisallowsChanges() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var subjectChangePolicy = mock(MeasurementSubjectChangePolicy.class);
    final var set = measurementSet();
    when(repository.findVersionedById(set.measurementSetId())).thenReturn(Optional.of(
        new VersionedAggregate<>(set, AggregateRevision.of(2))
    ));
    doThrow(new DomainConflictException(
        "feedstock batch measurements can be changed only when lifecycle is ACTIVE"
    )).when(subjectChangePolicy).requireChangesAllowed(set.subject());
    final var service = retractionService(repository, subjectAccessControl, subjectChangePolicy);

    assertThatThrownBy(() -> service.retract(new RetractMeasurementSetCommand(
        set.measurementSetId(),
        2,
        MeasurementRetractionReason.of("Invalidated measurement set")
    )))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("feedstock batch measurements can be changed only when lifecycle is ACTIVE");
    verify(repository, never()).appendRevision(any());
  }

  private static CreateMeasurementSetService createService(
      MeasurementSetRepository repository,
      MeasurementSubjectAccessControl subjectAccessControl,
      ObservedMaterialStateValidator observedMaterialStateValidator
  ) {
    return createService(
        repository,
        subjectAccessControl,
        mock(MeasurementSubjectChangePolicy.class),
        observedMaterialStateValidator
    );
  }

  private static CreateMeasurementSetService createService(
      MeasurementSetRepository repository,
      MeasurementSubjectAccessControl subjectAccessControl,
      MeasurementSubjectChangePolicy subjectChangePolicy,
      ObservedMaterialStateValidator observedMaterialStateValidator
  ) {
    final MeasurementActorProvider actorProvider = () -> ActorReference.accountUser(newUuid());
    return new CreateMeasurementSetService(
        repository,
        subjectAccessControl,
        subjectChangePolicy,
        (subject, measurements) -> {
        },
        actorProvider,
        observedMaterialStateValidator,
        new MeasurementSetRevisionFactory(),
        new DirectTransactionHelper(),
        Clock.fixed(CORRECTION_TIME, ZoneOffset.UTC)
    );
  }

  private static CorrectMeasurementSetService correctionService(
      MeasurementSetRepository repository,
      MeasurementSubjectAccessControl subjectAccessControl
  ) {
    return correctionService(
        repository,
        subjectAccessControl,
        mock(MeasurementSubjectChangePolicy.class)
    );
  }

  private static CorrectMeasurementSetService correctionService(
      MeasurementSetRepository repository,
      MeasurementSubjectAccessControl subjectAccessControl,
      MeasurementSubjectChangePolicy subjectChangePolicy
  ) {
    final MeasurementActorProvider actorProvider = () -> ActorReference.accountUser(newUuid());
    return new CorrectMeasurementSetService(
        repository,
        new MeasurementSetWriteAccess(repository, subjectAccessControl, subjectChangePolicy),
        (subject, measurements) -> {
        },
        actorProvider,
        new MeasurementSetRevisionFactory(),
        new DirectTransactionHelper(),
        Clock.fixed(CORRECTION_TIME, ZoneOffset.UTC)
    );
  }

  private static RetractMeasurementSetService retractionService(
      MeasurementSetRepository repository,
      MeasurementSubjectAccessControl subjectAccessControl
  ) {
    return retractionService(
        repository,
        subjectAccessControl,
        mock(MeasurementSubjectChangePolicy.class)
    );
  }

  private static RetractMeasurementSetService retractionService(
      MeasurementSetRepository repository,
      MeasurementSubjectAccessControl subjectAccessControl,
      MeasurementSubjectChangePolicy subjectChangePolicy
  ) {
    final MeasurementActorProvider actorProvider = () -> ActorReference.accountUser(newUuid());
    return new RetractMeasurementSetService(
        repository,
        new MeasurementSetWriteAccess(repository, subjectAccessControl, subjectChangePolicy),
        new MeasurementSetRevisionFactory(),
        actorProvider,
        new DirectTransactionHelper(),
        Clock.fixed(CORRECTION_TIME, ZoneOffset.UTC)
    );
  }

  private static CreateMeasurementSetCommand command(
      MeasurementSubjectReference subject,
      MaterialStateId observedStateId
  ) {
    return new CreateMeasurementSetCommand(
        null,
        subject,
        observedStateId,
        MeasurementSetName.of("Current pH"),
        null,
        List.of(direct(MeasurementCharacteristic.PH, "3.25", CORRECTION_TIME))
    );
  }

  private static MeasurementSet measurementSet() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var revision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.initial(),
        MaterialStateId.newId(),
        MeasurementSetName.of("Initial chemistry"),
        "Initial note",
        null,
        INITIAL_TIME,
        ActorReference.accountUser(newUuid()),
        List.of(
            result(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052"),
            result(MeasurementCharacteristic.PH, "3.40")
        ),
        List.of()
    );
    return MeasurementSet.create(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        subject,
        revision
    );
  }

  private static PatchMeasurementSetCommand patchCommand(
      MeasurementSet set,
      MeasurementCorrectionReason reason,
      List<MeasurementCharacteristicInput> replacements,
      Set<MeasurementCharacteristic> removals
  ) {
    return new PatchMeasurementSetCommand(
        set.measurementSetId(),
        0,
        null,
        false,
        null,
        reason,
        replacements,
        removals
    );
  }

  private static MeasurementRecord result(
      MeasurementCharacteristic characteristic,
      String value
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        characteristic,
        MeasurementRecordRole.RESULT,
        PointCharacteristicValue.of(value(characteristic, new BigDecimal(value))),
        INITIAL_TIME,
        PROVENANCE,
        null,
        List.of()
    );
  }

  private static MeasurementCharacteristicInput direct(
      MeasurementCharacteristic characteristic,
      String value,
      Instant measuredAt
  ) {
    return MeasurementCharacteristicInput.direct(
        characteristic,
        PointCharacteristicValue.of(value(characteristic, new BigDecimal(value))),
        measuredAt,
        PROVENANCE
    );
  }

  private static final class DirectTransactionHelper implements TransactionHelper {

    @Override
    public <T> @NonNull T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
      return supplier.get();
    }

    @Override
    public void executeInTransaction(@NonNull Runnable runnable) {
      runnable.run();
    }
  }
}
