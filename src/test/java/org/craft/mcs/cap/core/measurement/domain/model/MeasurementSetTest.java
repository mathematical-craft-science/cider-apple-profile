package org.craft.mcs.cap.core.measurement.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;


class MeasurementSetTest {

  private static final Instant INITIAL_TIME = Instant.parse("2026-07-20T10:00:00Z");
  private static final MaterialStateId MATERIAL_STATE_ID = MaterialStateId.newId();
  private static final MeasurementRetractionReason RETRACTION_REASON =
      MeasurementRetractionReason.of("Invalidated measurement set");

  @Test
  void derivesAggregateCreationAndUpdateTimeFromInitialRevision() {
    final var measurementSetId = MeasurementSetId.of(newUuid());
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject();
    final var initialRevision = revision(1, INITIAL_TIME);
    final var set = MeasurementSet.create(
        measurementSetId,
        workspaceId,
        subject,
        initialRevision
    );

    assertThat(set)
        .returns(measurementSetId, MeasurementSet::measurementSetId)
        .returns(workspaceId, MeasurementSet::workspaceId)
        .returns(subject, MeasurementSet::subject)
        .returns(initialRevision, MeasurementSet::currentRevision)
        .returns(INITIAL_TIME, MeasurementSet::createdAt)
        .returns(INITIAL_TIME, MeasurementSet::updatedAt)
        .returns(MeasurementSetLifecycle.ACTIVE, MeasurementSet::lifecycle)
        .returns(null, MeasurementSet::retraction);
  }

  @Test
  void restoresPersistedAggregateState() {
    final var measurementSetId = MeasurementSetId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject();
    final var currentRevision = revision(2, INITIAL_TIME.plusSeconds(60));
    final var updatedAt = INITIAL_TIME.plusSeconds(120);
    final var retraction = new MeasurementSetRetraction(
        ActorReference.accountUser(newUuid()),
        updatedAt,
        RETRACTION_REASON
    );

    final var restored = MeasurementSet.restore(
        measurementSetId,
        workspaceId,
        subject,
        MeasurementSetLifecycle.RETRACTED,
        retraction,
        currentRevision,
        INITIAL_TIME,
        updatedAt
    );

    assertThat(restored)
        .returns(measurementSetId, MeasurementSet::measurementSetId)
        .returns(workspaceId, MeasurementSet::workspaceId)
        .returns(subject, MeasurementSet::subject)
        .returns(MeasurementSetLifecycle.RETRACTED, MeasurementSet::lifecycle)
        .returns(retraction, MeasurementSet::retraction)
        .returns(currentRevision, MeasurementSet::currentRevision)
        .returns(INITIAL_TIME, MeasurementSet::createdAt)
        .returns(updatedAt, MeasurementSet::updatedAt);
  }

  @Test
  void restoredLifecycleRequiresConsistentRetractionAudit() {
    final var revision = revision(2, INITIAL_TIME.plusSeconds(60));
    final var updatedAt = INITIAL_TIME.plusSeconds(120);
    final var retraction = new MeasurementSetRetraction(
        ActorReference.accountUser(newUuid()),
        updatedAt,
        RETRACTION_REASON
    );

    assertThatThrownBy(() -> MeasurementSet.restore(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        subject(),
        MeasurementSetLifecycle.ACTIVE,
        retraction,
        revision,
        INITIAL_TIME,
        updatedAt
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active measurement set cannot have retraction audit");
    assertThatThrownBy(() -> MeasurementSet.restore(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        subject(),
        MeasurementSetLifecycle.RETRACTED,
        null,
        revision,
        INITIAL_TIME,
        updatedAt
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("retracted measurement set requires retraction audit");
  }

  @Test
  void restoredRetractionTimeMustEqualAggregateUpdateTime() {
    final var revision = revision(2, INITIAL_TIME.plusSeconds(60));
    final var updatedAt = INITIAL_TIME.plusSeconds(120);

    assertThatThrownBy(() -> MeasurementSet.restore(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        subject(),
        MeasurementSetLifecycle.RETRACTED,
        new MeasurementSetRetraction(
            ActorReference.accountUser(newUuid()),
            updatedAt.minusSeconds(1),
            RETRACTION_REASON
        ),
        revision,
        INITIAL_TIME,
        updatedAt
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement-set retraction time must equal aggregate update time");
  }

  @Test
  void appliesCorrectionTimeFromTheAppendedRevision() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var correction = revision(2, correctionTime);

    set.correct(correction);

    assertThat(set)
        .returns(correction, MeasurementSet::currentRevision)
        .returns(correctionTime, MeasurementSet::updatedAt);
  }

  @Test
  void correctionRequiresANewRevisionIdentity() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var current = set.currentRevision();
    final var correction = new MeasurementSetRevision(
        current.revisionId(),
        current.revisionNumber().next(),
        current.materialStateId(),
        current.name(),
        current.note(),
        MeasurementCorrectionReason.of("Corrected result"),
        INITIAL_TIME.plusSeconds(60),
        ActorReference.accountUser(newUuid()),
        current.records(),
        current.resultRetractions()
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement correction requires a new revision identity");
  }

  @Test
  void rejectsCorrectionThatPredatesCurrentAggregateState() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var backdatedCorrection = revision(2, INITIAL_TIME.minusSeconds(1));

    assertThatThrownBy(() -> set.correct(backdatedCorrection))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement correction cannot predate the current aggregate state");
  }

  @Test
  void requiresInitialRevisionNumberWhenCreatingSet() {
    final var nonInitialRevision = revision(2, INITIAL_TIME);

    assertThatThrownBy(() -> createSet(nonInitialRevision))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("initial measurement revision number must be one");
  }

  @Test
  void correctionMustAppendNextRevisionNumber() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var skippedRevision = revision(3, INITIAL_TIME.plusSeconds(60));

    assertThatThrownBy(() -> set.correct(skippedRevision))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement correction must append the next revision");
  }

  @Test
  void correctionMustPreserveObservedMaterialState() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var correction = revision(
        2,
        MaterialStateId.newId(),
        INITIAL_TIME.plusSeconds(60)
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement correction must preserve the observed material state");
  }

  @Test
  void correctionRequiresTombstoneForEveryRemovedResult() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var correction = correctedRevision(
        List.of(result(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050", correctionTime)),
        List.of(),
        correctionTime
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("removed measurement results require retained retraction tombstones");
  }

  @Test
  void correctionRetainsRetractionUntilReplacementEvidenceIsRecorded() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var retractionTime = INITIAL_TIME.plusSeconds(60);
    set.correct(retractionRevision(
        set.currentRevision(),
        activeResult(set, MeasurementCharacteristic.PH),
        retractionTime,
        2
    ));
    final var nextCorrectionTime = retractionTime.plusSeconds(60);
    final var discardedRetraction = correctedRevision(
        List.of(result(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050", nextCorrectionTime)),
        List.of(),
        nextCorrectionTime,
        3
    );

    assertThatThrownBy(() -> set.correct(discardedRetraction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement result retractions must remain until replacement evidence is recorded");
  }

  @Test
  void correctionCannotRetractAResultThatWasNeverRepresented() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var correction = correctedRevision(
        List.of(result(MeasurementCharacteristic.PH, "3.40", correctionTime)),
        List.of(retraction(
            set.currentRevision(),
            result(MeasurementCharacteristic.TANNIN, "1200", INITIAL_TIME),
            correctionTime
        )),
        correctionTime
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("only a previously represented measurement result can be retracted");
  }

  @ParameterizedTest
  @EnumSource(RetractionAuditMismatch.class)
  void newRetractionAuditMustMatchItsCorrectionRevision(RetractionAuditMismatch mismatch) {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var correctionActor = ActorReference.accountUser(newUuid());
    final var correctionReason = MeasurementCorrectionReason.of("Invalidated result");
    final var retraction = new MeasurementResultRetraction(
        set.currentRevision().revisionId(),
        set.currentRevision().revisionNumber(),
        activeResult(set, MeasurementCharacteristic.PH),
        List.of(),
        mismatch == RetractionAuditMismatch.ACTOR
            ? ActorReference.accountUser(newUuid())
            : correctionActor,
        mismatch == RetractionAuditMismatch.TIMESTAMP
            ? correctionTime.minusSeconds(1)
            : correctionTime,
        mismatch == RetractionAuditMismatch.REASON
            ? MeasurementCorrectionReason.of("Different reason")
            : correctionReason
    );
    final var correction = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        MATERIAL_STATE_ID,
        MeasurementSetName.of("Corrected bench measurement"),
        null,
        correctionReason,
        correctionTime,
        correctionActor,
        List.of(),
        List.of(retraction)
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("new measurement result retraction audit must match its correction revision");
  }

  @Test
  void correctionCannotRetractDifferentEvidenceForTheSameCharacteristic() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Invalidated result");
    final var correction = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(2),
        MATERIAL_STATE_ID,
        MeasurementSetName.of("Corrected bench measurement"),
        null,
        reason,
        correctionTime,
        actor,
        List.of(),
        List.of(new MeasurementResultRetraction(
            set.currentRevision().revisionId(),
            set.currentRevision().revisionNumber(),
            result(MeasurementCharacteristic.PH, "3.40", INITIAL_TIME),
            List.of(),
            actor,
            correctionTime,
            reason
        ))
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement result retraction must preserve the current active result evidence");
  }

  @Test
  void correctionCannotRebindExistingEvidenceIdentityToDifferentEvidence() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var currentResult = activeResult(set, MeasurementCharacteristic.PH);
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var reboundResult = result(
        currentResult.recordId(),
        MeasurementCharacteristic.PH,
        "3.50",
        INITIAL_TIME
    );
    final var correction = correctedRevision(
        List.of(reboundResult),
        List.of(),
        correctionTime
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement evidence identity cannot be rebound to different evidence");
  }

  @Test
  void retractedResultRequiresReplacementEvidenceWithNewIdentity() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var retractedResult = activeResult(set, MeasurementCharacteristic.PH);
    final var retractionTime = INITIAL_TIME.plusSeconds(60);
    set.correct(retractionRevision(
        set.currentRevision(),
        retractedResult,
        retractionTime,
        2
    ));
    final var replacement = correctedRevision(
        List.of(retractedResult),
        List.of(),
        retractionTime.plusSeconds(60),
        3
    );

    assertThatThrownBy(() -> set.correct(replacement))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("retracted measurement result requires replacement evidence with a new identity");
  }

  @Test
  void retractionCannotAlterObservationEvidenceSnapshot() {
    final var firstObservation = observation("1.050", INITIAL_TIME.minusSeconds(60));
    final var secondObservation = observation("1.052", INITIAL_TIME);
    final var representativeResult = arithmeticMeanResult(firstObservation, secondObservation);
    final var sourceRevision = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.initial(),
        MATERIAL_STATE_ID,
        MeasurementSetName.of("Repeated SG measurement"),
        null,
        null,
        INITIAL_TIME,
        ActorReference.accountUser(newUuid()),
        List.of(firstObservation, secondObservation, representativeResult),
        List.of()
    );
    final var set = createSet(sourceRevision);
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Invalid hydrometer reading");
    final var alteredObservation = observation(
        firstObservation.recordId(),
        "1.049",
        firstObservation.measuredAt()
    );
    final var correction = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        sourceRevision.revisionNumber().next(),
        MATERIAL_STATE_ID,
        sourceRevision.name(),
        null,
        reason,
        correctionTime,
        actor,
        List.of(),
        List.of(new MeasurementResultRetraction(
            sourceRevision.revisionId(),
            sourceRevision.revisionNumber(),
            representativeResult,
            List.of(alteredObservation, secondObservation),
            actor,
            correctionTime,
            reason
        ))
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement result retraction must preserve its current observation evidence");
  }

  @Test
  void retractsDerivedResultWithCompleteObservationEvidenceRegardlessOfRecordOrder() {
    final var firstObservation = observation("1.050", INITIAL_TIME.minusSeconds(60));
    final var secondObservation = observation("1.052", INITIAL_TIME);
    final var representativeResult = arithmeticMeanResult(firstObservation, secondObservation);
    final var sourceRevision = initialRevision(
        List.of(representativeResult, firstObservation, secondObservation)
    );
    final var set = createSet(sourceRevision);
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Invalid repeated measurement");
    final var retraction = new MeasurementResultRetraction(
        sourceRevision.revisionId(),
        sourceRevision.revisionNumber(),
        representativeResult,
        List.of(firstObservation, secondObservation),
        actor,
        correctionTime,
        reason
    );

    set.correct(correctionRevision(
        sourceRevision.revisionNumber().next(),
        correctionTime,
        actor,
        reason,
        List.of(),
        List.of(retraction)
    ));

    assertThat(set.currentRevision().resultRetractions())
        .containsExactly(retraction);
  }

  @Test
  void carriesResultRetractionUnchangedUntilReplacementEvidenceIsRecorded() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var retractionTime = INITIAL_TIME.plusSeconds(60);
    set.correct(retractionRevision(
        set.currentRevision(),
        activeResult(set, MeasurementCharacteristic.PH),
        retractionTime,
        2
    ));
    final var retainedRetraction = set.currentRevision().resultRetractions().getFirst();
    final var correctionTime = retractionTime.plusSeconds(60);

    set.correct(correctedRevision(
        List.of(),
        List.of(retainedRetraction),
        correctionTime,
        3
    ));

    assertThat(set.currentRevision().resultRetractions())
        .containsExactly(retainedRetraction);
  }

  @Test
  void carriedResultRetractionCannotBeChanged() {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var retractionTime = INITIAL_TIME.plusSeconds(60);
    set.correct(retractionRevision(
        set.currentRevision(),
        activeResult(set, MeasurementCharacteristic.PH),
        retractionTime,
        2
    ));
    final var retainedRetraction = set.currentRevision().resultRetractions().getFirst();
    final var changedRetraction = new MeasurementResultRetraction(
        retainedRetraction.sourceRevisionId(),
        retainedRetraction.sourceRevisionNumber(),
        retainedRetraction.result(),
        retainedRetraction.observations(),
        ActorReference.accountUser(newUuid()),
        retainedRetraction.retractedAt(),
        retainedRetraction.reason()
    );

    assertThatThrownBy(() -> set.correct(correctedRevision(
        List.of(),
        List.of(changedRetraction),
        retractionTime.plusSeconds(60),
        3
    )))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("carried measurement result retraction must remain immutable");
  }

  @Test
  void replacementCannotReuseAnObservationIdentityRetainedByRetraction() {
    final var firstObservation = observation("1.050", INITIAL_TIME.minusSeconds(60));
    final var secondObservation = observation("1.052", INITIAL_TIME);
    final var representativeResult = arithmeticMeanResult(firstObservation, secondObservation);
    final var sourceRevision = initialRevision(
        List.of(firstObservation, secondObservation, representativeResult)
    );
    final var set = createSet(sourceRevision);
    final var retractionTime = INITIAL_TIME.plusSeconds(60);
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Invalid repeated measurement");
    final var retraction = new MeasurementResultRetraction(
        sourceRevision.revisionId(),
        sourceRevision.revisionNumber(),
        representativeResult,
        List.of(firstObservation, secondObservation),
        actor,
        retractionTime,
        reason
    );
    set.correct(correctionRevision(
        sourceRevision.revisionNumber().next(),
        retractionTime,
        actor,
        reason,
        List.of(),
        List.of(retraction)
    ));

    final var reboundObservation = observation(
        firstObservation.recordId(),
        "1.049",
        firstObservation.measuredAt()
    );
    final var newObservation = observation("1.053", secondObservation.measuredAt());
    final var replacementResult = arithmeticMeanResult(reboundObservation, newObservation);

    assertThatThrownBy(() -> set.correct(correctedRevision(
        List.of(reboundObservation, newObservation, replacementResult),
        List.of(),
        retractionTime.plusSeconds(60),
        3
    )))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement evidence identity cannot be rebound to different evidence");
  }

  @ParameterizedTest
  @EnumSource(RetractionSourceMismatch.class)
  void newRetractionMustReferenceTheCurrentSourceRevision(RetractionSourceMismatch mismatch) {
    final var set = createSet(revision(1, INITIAL_TIME));
    final var sourceRevision = set.currentRevision();
    final var correctionTime = INITIAL_TIME.plusSeconds(60);
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Invalidated result");
    final var retraction = new MeasurementResultRetraction(
        mismatch == RetractionSourceMismatch.ID
            ? MeasurementSetRevisionId.newId()
            : sourceRevision.revisionId(),
        mismatch == RetractionSourceMismatch.NUMBER
            ? sourceRevision.revisionNumber().next()
            : sourceRevision.revisionNumber(),
        activeResult(set, MeasurementCharacteristic.PH),
        List.of(),
        actor,
        correctionTime,
        reason
    );
    final var correction = new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        sourceRevision.revisionNumber().next(),
        MATERIAL_STATE_ID,
        sourceRevision.name(),
        null,
        reason,
        correctionTime,
        actor,
        List.of(),
        List.of(retraction)
    );

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement result retraction must reference the current source revision");
  }

  @Test
  void rejectsRetractionThatPredatesCurrentAggregateState() {
    final var set = createSet(revision(1, INITIAL_TIME));

    assertThatThrownBy(() -> set.retract(
        ActorReference.accountUser(newUuid()),
        INITIAL_TIME.minusSeconds(1),
        RETRACTION_REASON
    ))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement retraction cannot predate the current aggregate state");
  }

  @Test
  void rejectsRestoredRevisionOutsideAggregateLifetime() {
    final var revision = revision(2, INITIAL_TIME.plusSeconds(120));

    assertThatThrownBy(() -> MeasurementSet.restore(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        subject(),
        MeasurementSetLifecycle.ACTIVE,
        null,
        revision,
        INITIAL_TIME,
        INITIAL_TIME.plusSeconds(60)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("current measurement revision time must belong to the aggregate lifetime");
  }

  @Test
  void rejectsAggregateCreationTimeAfterUpdateTime() {
    final var revision = revision(1, INITIAL_TIME);

    assertThatThrownBy(() -> MeasurementSet.restore(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        subject(),
        MeasurementSetLifecycle.ACTIVE,
        null,
        revision,
        INITIAL_TIME.plusSeconds(1),
        INITIAL_TIME
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement set creation time cannot follow its update time");
  }

  @Test
  void retractedSetCannotBeCorrected() {
    final var set = createSet(revision(1, INITIAL_TIME));
    set.retract(
        ActorReference.accountUser(newUuid()),
        INITIAL_TIME.plusSeconds(30),
        RETRACTION_REASON
    );
    final var correction = revision(2, INITIAL_TIME.plusSeconds(60));

    assertThatThrownBy(() -> set.correct(correction))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("retracted measurement set cannot be changed");
  }

  @ParameterizedTest
  @EnumSource(MeasurementSetMutation.class)
  void activeSetAllowsDeclaredLifecycleMutations(MeasurementSetMutation mutation) {
    final var set = createSet(revision(1, INITIAL_TIME));

    mutation.apply(set);

    assertThat(set.lifecycle()).isEqualTo(mutation.resultingLifecycle());
  }

  @ParameterizedTest
  @EnumSource(MeasurementSetMutation.class)
  void retractedSetRejectsEveryFurtherLifecycleMutation(MeasurementSetMutation mutation) {
    final var set = createSet(revision(1, INITIAL_TIME));
    set.retract(
        ActorReference.accountUser(newUuid()),
        INITIAL_TIME.plusSeconds(30),
        RETRACTION_REASON
    );

    assertThatThrownBy(() -> mutation.apply(set))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("retracted measurement set cannot be changed");
  }

  private static MeasurementSet createSet(MeasurementSetRevision revision) {
    return MeasurementSet.create(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        subject(),
        revision
    );
  }

  private static MeasurementSetRevision revision(
      int revisionNumber,
      Instant createdAt
  ) {
    return revision(revisionNumber, MATERIAL_STATE_ID, createdAt);
  }

  private static MeasurementSetRevision revision(
      int revisionNumber,
      MaterialStateId materialStateId,
      Instant createdAt
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(revisionNumber),
        materialStateId,
        MeasurementSetName.of("Bench measurement"),
        null,
        revisionNumber == 1 ? null : MeasurementCorrectionReason.of("Corrected result"),
        createdAt,
        ActorReference.accountUser(newUuid()),
        List.of(result(MeasurementCharacteristic.PH, "3.40", createdAt)),
        List.of()
    );
  }

  private static MeasurementSetRevision correctedRevision(
      List<MeasurementRecord> records,
      List<MeasurementResultRetraction> retractions,
      Instant createdAt
  ) {
    return correctedRevision(records, retractions, createdAt, 2);
  }

  private static MeasurementSetRevision initialRevision(
      List<MeasurementRecord> records
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.initial(),
        MATERIAL_STATE_ID,
        MeasurementSetName.of("Initial repeated measurement"),
        null,
        null,
        INITIAL_TIME,
        ActorReference.accountUser(newUuid()),
        records,
        List.of()
    );
  }

  private static MeasurementSetRevision correctionRevision(
      MeasurementSetRevisionNumber revisionNumber,
      Instant createdAt,
      ActorReference actor,
      MeasurementCorrectionReason reason,
      List<MeasurementRecord> records,
      List<MeasurementResultRetraction> retractions
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        revisionNumber,
        MATERIAL_STATE_ID,
        MeasurementSetName.of("Corrected repeated measurement"),
        null,
        reason,
        createdAt,
        actor,
        records,
        retractions
    );
  }

  private static MeasurementSetRevision retractionRevision(
      MeasurementSetRevision sourceRevision,
      MeasurementRecord result,
      Instant createdAt,
      int revisionNumber
  ) {
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Invalidated result");
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(revisionNumber),
        MATERIAL_STATE_ID,
        MeasurementSetName.of("Corrected bench measurement"),
        null,
        reason,
        createdAt,
        actor,
        List.of(),
        List.of(new MeasurementResultRetraction(
            sourceRevision.revisionId(),
            sourceRevision.revisionNumber(),
            result,
            List.of(),
            actor,
            createdAt,
            reason
        ))
    );
  }

  private static MeasurementSetRevision correctedRevision(
      List<MeasurementRecord> records,
      List<MeasurementResultRetraction> retractions,
      Instant createdAt,
      int revisionNumber
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(revisionNumber),
        MATERIAL_STATE_ID,
        MeasurementSetName.of("Corrected bench measurement"),
        null,
        MeasurementCorrectionReason.of("Corrected result"),
        createdAt,
        ActorReference.accountUser(newUuid()),
        records,
        retractions
    );
  }

  private static MeasurementResultRetraction retraction(
      MeasurementSetRevision sourceRevision,
      MeasurementRecord result,
      Instant retractedAt
  ) {
    return new MeasurementResultRetraction(
        sourceRevision.revisionId(),
        sourceRevision.revisionNumber(),
        result,
        List.of(),
        ActorReference.accountUser(newUuid()),
        retractedAt,
        MeasurementCorrectionReason.of("Invalidated result")
    );
  }

  private static MeasurementRecord activeResult(
      MeasurementSet set,
      MeasurementCharacteristic characteristic
  ) {
    return set.currentRevision().records().stream()
        .filter(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
        .filter(measurementRecord -> measurementRecord.characteristic() == characteristic)
        .findFirst()
        .orElseThrow();
  }

  private static MeasurementRecord result(
      MeasurementCharacteristic characteristic,
      String measuredValue,
      Instant measuredAt
  ) {
    return result(MeasurementRecordId.newId(), characteristic, measuredValue, measuredAt);
  }

  private static MeasurementRecord result(
      MeasurementRecordId recordId,
      MeasurementCharacteristic characteristic,
      String measuredValue,
      Instant measuredAt
  ) {
    return new MeasurementRecord(
        recordId,
        characteristic,
        MeasurementRecordRole.RESULT,
        PointCharacteristicValue.of(value(characteristic, new BigDecimal(measuredValue))),
        measuredAt,
        new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null),
        null,
        List.of()
    );
  }

  private static MeasurementRecord observation(
      String measuredValue,
      Instant measuredAt
  ) {
    return observation(MeasurementRecordId.newId(), measuredValue, measuredAt);
  }

  private static MeasurementRecord observation(
      MeasurementRecordId recordId,
      String measuredValue,
      Instant measuredAt
  ) {
    return new MeasurementRecord(
        recordId,
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementRecordRole.OBSERVATION,
        PointCharacteristicValue.of(value(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            new BigDecimal(measuredValue)
        )),
        measuredAt,
        new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null),
        null,
        List.of()
    );
  }

  private static MeasurementRecord arithmeticMeanResult(
      MeasurementRecord firstObservation,
      MeasurementRecord secondObservation
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementRecordRole.RESULT,
        PointCharacteristicValue.of(value(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            new BigDecimal("1.051")
        )),
        secondObservation.measuredAt(),
        MeasurementProvenance.derived(),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(firstObservation.recordId(), secondObservation.recordId())
    );
  }

  private static MeasurementSubjectReference subject() {
    return new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid());
  }

  private enum MeasurementSetMutation {
    CORRECT(MeasurementSetLifecycle.ACTIVE),
    RETRACT(MeasurementSetLifecycle.RETRACTED);

    private final MeasurementSetLifecycle resultingLifecycle;

    MeasurementSetMutation(MeasurementSetLifecycle resultingLifecycle) {
      this.resultingLifecycle = resultingLifecycle;
    }

    void apply(MeasurementSet set) {
      switch (this) {
        case CORRECT -> set.correct(revision(2, INITIAL_TIME.plusSeconds(30)));
        case RETRACT -> set.retract(
            ActorReference.accountUser(newUuid()),
            INITIAL_TIME.plusSeconds(30),
            RETRACTION_REASON
        );
      }
    }

    MeasurementSetLifecycle resultingLifecycle() {
      return resultingLifecycle;
    }
  }

  private enum RetractionAuditMismatch {
    ACTOR,
    TIMESTAMP,
    REASON
  }

  private enum RetractionSourceMismatch {
    ID,
    NUMBER
  }
}
