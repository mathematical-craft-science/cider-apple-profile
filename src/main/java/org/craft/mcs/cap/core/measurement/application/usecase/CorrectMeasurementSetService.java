package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.in.CorrectMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.PatchMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.ReplaceMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementActorProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectMeasurementPolicy;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.measurement.application.usecase.MeasurementSetViewMapper.toView;


/** Appends immutable replacement or partial-correction revisions to a measurement set. */
@Component
@RequiredArgsConstructor
public final class CorrectMeasurementSetService implements CorrectMeasurementSetUseCase {

  private final @NonNull MeasurementSetRepository repository;
  private final @NonNull MeasurementSetWriteAccess writeAccess;
  private final @NonNull MeasurementSubjectMeasurementPolicy subjectMeasurementPolicy;
  private final @NonNull MeasurementActorProvider actorProvider;
  private final @NonNull MeasurementSetRevisionFactory revisionFactory;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull MeasurementSetView replace(@NonNull ReplaceMeasurementSetCommand command) {
    requireNonNull(command, "replace measurement set command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var state = writeAccess.require(command.measurementSetId(), command.expectedVersion());
      final var set = state.aggregate();
      subjectMeasurementPolicy.validate(set.subject(), command.measurements());
      final var now = clock.instant();
      set.correct(revisionFactory.replacement(
          set.currentRevision(),
          command,
          now,
          actorProvider.requireCurrentActorReference()
      ));
      return toView(repository.appendRevision(state.withAggregate(set)));
    });
  }

  @Override
  public @NonNull MeasurementSetView patch(@NonNull PatchMeasurementSetCommand command) {
    requireNonNull(command, "patch measurement set command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var state = writeAccess.require(command.measurementSetId(), command.expectedVersion());
      final var set = state.aggregate();
      subjectMeasurementPolicy.validate(set.subject(), command.replacements());
      final var now = clock.instant();
      set.correct(revisionFactory.patch(
          set.currentRevision(),
          command,
          now,
          actorProvider.requireCurrentActorReference()
      ));
      return toView(repository.appendRevision(state.withAggregate(set)));
    });
  }
}
