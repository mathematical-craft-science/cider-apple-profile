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
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementActorProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementMaterialStateProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevision;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
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
  void createAttachesEvidenceToExpectedCurrentMaterialState() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var materialStateProvider = mock(MeasurementMaterialStateProvider.class);
    final var workspaceId = WorkspaceId.newId();
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var expectedStateId = MaterialStateId.newId();
    when(subjectAccessControl.requireWriteAccess(subject)).thenReturn(workspaceId);
    when(materialStateProvider.requireCurrentState(subject, expectedStateId)).thenReturn(expectedStateId);
    when(repository.create(any())).thenAnswer(invocation -> new VersionedAggregate<>(
        invocation.getArgument(0),
        AggregateRevision.of(0)
    ));
    final var service = createService(
        repository,
        subjectAccessControl,
        materialStateProvider
    );

    final var result = service.create(command(subject, expectedStateId));

    assertThat(result)
        .returns(subject, MeasurementSetView::subject)
        .returns(expectedStateId, MeasurementSetView::materialStateId)
        .satisfies(view -> assertThat(view.records()).hasSize(1));
    verify(subjectAccessControl).requireWriteAccess(subject);
  }

  @Test
  void createRejectsStaleExpectedStateBeforePersistingEvidence() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var materialStateProvider = mock(MeasurementMaterialStateProvider.class);
    final var workspaceId = WorkspaceId.newId();
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var expectedStateId = MaterialStateId.newId();
    when(subjectAccessControl.requireWriteAccess(subject)).thenReturn(workspaceId);
    when(materialStateProvider.requireCurrentState(subject, expectedStateId))
        .thenThrow(new ConcurrentResourceModificationException("current material state", null));
    final var service = createService(
        repository,
        subjectAccessControl,
        materialStateProvider
    );

    assertThatThrownBy(() -> service.create(command(subject, expectedStateId)))
        .isInstanceOf(ConcurrentResourceModificationException.class);
    verify(repository, never()).create(any());
  }

  @Test
  void patchAppendsRevisionAndCopiesUnchangedEvidenceWithNewRecordIdentity() {
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
                .satisfies(value -> assertThat(value.recordId()).isNotEqualTo(originalSugar.recordId())))
            .anySatisfy(recordView -> assertThat(recordView)
                .returns(MeasurementCharacteristic.PH, MeasurementRecordView::characteristic)
                .returns(CORRECTION_TIME, MeasurementRecordView::measuredAt)
                .satisfies(value -> assertThat(pointValue(value.value()).asBigDecimal())
                    .isEqualByComparingTo("3.25"))));
    verify(subjectAccessControl).requireWriteAccess(set.subject());
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
  void rejectsNegativeExpectedVersionBeforeCallingPersistence() {
    final var measurementSetId = MeasurementSetId.newId();

    assertThatThrownBy(() -> new RetractMeasurementSetCommand(measurementSetId, -1))
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
  void retractPreservesEvidenceAndChangesAggregateLifecycle() {
    final var repository = mock(MeasurementSetRepository.class);
    final var subjectAccessControl = mock(MeasurementSubjectAccessControl.class);
    final var set = measurementSet();
    final var state = new VersionedAggregate<>(set, AggregateRevision.of(2));
    when(repository.findVersionedById(set.measurementSetId())).thenReturn(Optional.of(state));
    when(repository.retract(state)).thenReturn(state);
    final var service = retractionService(repository, subjectAccessControl);

    service.retract(new RetractMeasurementSetCommand(set.measurementSetId(), 2));

    assertThat(set)
        .returns(MeasurementSetLifecycle.RETRACTED, MeasurementSet::lifecycle)
        .satisfies(retracted -> assertThat(retracted.currentRevision().records()).hasSize(2));
    verify(subjectAccessControl).requireWriteAccess(set.subject());
    verify(repository).retract(state);
  }

  private static CreateMeasurementSetService createService(
      MeasurementSetRepository repository,
      MeasurementSubjectAccessControl subjectAccessControl,
      MeasurementMaterialStateProvider materialStateProvider
  ) {
    final MeasurementActorProvider actorProvider = () -> ActorReference.accountUser(newUuid());
    return new CreateMeasurementSetService(
        repository,
        subjectAccessControl,
        actorProvider,
        materialStateProvider,
        new MeasurementSetRevisionFactory(),
        new DirectTransactionHelper(),
        Clock.fixed(CORRECTION_TIME, ZoneOffset.UTC)
    );
  }

  private static CorrectMeasurementSetService correctionService(
      MeasurementSetRepository repository,
      MeasurementSubjectAccessControl subjectAccessControl
  ) {
    final MeasurementActorProvider actorProvider = () -> ActorReference.accountUser(newUuid());
    return new CorrectMeasurementSetService(
        repository,
        new MeasurementSetWriteAccess(repository, subjectAccessControl),
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
    return new RetractMeasurementSetService(
        repository,
        new MeasurementSetWriteAccess(repository, subjectAccessControl),
        new DirectTransactionHelper(),
        Clock.fixed(CORRECTION_TIME, ZoneOffset.UTC)
    );
  }

  private static CreateMeasurementSetCommand command(
      MeasurementSubjectReference subject,
      MaterialStateId expectedStateId
  ) {
    return new CreateMeasurementSetCommand(
        null,
        subject,
        expectedStateId,
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
        )
    );
    return MeasurementSet.create(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        subject,
        revision,
        INITIAL_TIME
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
