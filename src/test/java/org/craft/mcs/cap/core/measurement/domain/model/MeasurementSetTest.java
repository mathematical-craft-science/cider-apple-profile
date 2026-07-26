package org.craft.mcs.cap.core.measurement.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
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
        .returns(MeasurementSetLifecycle.ACTIVE, MeasurementSet::lifecycle);
  }

  @Test
  void restoresPersistedAggregateState() {
    final var measurementSetId = MeasurementSetId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject();
    final var currentRevision = revision(2, INITIAL_TIME.plusSeconds(60));
    final var updatedAt = INITIAL_TIME.plusSeconds(120);

    final var restored = MeasurementSet.restore(
        measurementSetId,
        workspaceId,
        subject,
        MeasurementSetLifecycle.RETRACTED,
        currentRevision,
        INITIAL_TIME,
        updatedAt
    );

    assertThat(restored)
        .returns(measurementSetId, MeasurementSet::measurementSetId)
        .returns(workspaceId, MeasurementSet::workspaceId)
        .returns(subject, MeasurementSet::subject)
        .returns(MeasurementSetLifecycle.RETRACTED, MeasurementSet::lifecycle)
        .returns(currentRevision, MeasurementSet::currentRevision)
        .returns(INITIAL_TIME, MeasurementSet::createdAt)
        .returns(updatedAt, MeasurementSet::updatedAt);
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
        .isInstanceOf(DomainConflictException.class)
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
  void rejectsRetractionThatPredatesCurrentAggregateState() {
    final var set = createSet(revision(1, INITIAL_TIME));

    assertThatThrownBy(() -> set.retract(INITIAL_TIME.minusSeconds(1)))
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
        revision,
        INITIAL_TIME,
        INITIAL_TIME.plusSeconds(60)
    ))
        .isInstanceOf(DomainConflictException.class)
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
        revision,
        INITIAL_TIME.plusSeconds(1),
        INITIAL_TIME
    ))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("measurement set creation time cannot follow its update time");
  }

  @Test
  void retractedSetCannotBeCorrected() {
    final var set = createSet(revision(1, INITIAL_TIME));
    set.retract(INITIAL_TIME.plusSeconds(30));
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
    set.retract(INITIAL_TIME.plusSeconds(30));

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
        List.of(new MeasurementRecord(
            MeasurementRecordId.newId(),
            MeasurementCharacteristic.PH,
            MeasurementRecordRole.RESULT,
            PointCharacteristicValue.of(value(MeasurementCharacteristic.PH, new BigDecimal("3.40"))),
            createdAt,
            new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null),
            null,
            List.of()
        ))
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
        case RETRACT -> set.retract(INITIAL_TIME.plusSeconds(30));
      }
    }

    MeasurementSetLifecycle resultingLifecycle() {
      return resultingLifecycle;
    }
  }
}
